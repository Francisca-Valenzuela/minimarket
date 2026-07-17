package com.minimarket;

import com.minimarket.dto.DisponibilidadDTO;
import com.minimarket.entity.Producto;
import com.minimarket.entity.StockSucursal;
import com.minimarket.entity.Sucursal;
import com.minimarket.exception.InsufficientStockException;
import com.minimarket.exception.ResourceNotFoundException;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.StockSucursalRepository;
import com.minimarket.repository.SucursalRepository;
import com.minimarket.service.OrdenCompraService;
import com.minimarket.service.impl.SucursalServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SucursalServiceTest {

    @Mock
    private SucursalRepository sucursalRepository;

    @Mock
    private StockSucursalRepository stockSucursalRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private OrdenCompraService ordenCompraService;

    @InjectMocks
    private SucursalServiceImpl sucursalService;

    private Sucursal sucursal;
    private Producto producto;

    @BeforeEach
    void setUp() {
        sucursal = new Sucursal();
        sucursal.setId(1L);
        sucursal.setNombre("Sucursal Centro");
        sucursal.setDireccion("Av. Principal 123");
        sucursal.setComuna("Santiago");
        sucursal.setActiva(true);

        producto = new Producto();
        producto.setId(10L);
        producto.setNombre("Leche Entera 1L");
        producto.setPrecio(1200.0);
    }

    // ---------------------------------------------------------
    // CRUD de sucursal
    // ---------------------------------------------------------

    @Test
    void testListarTodas() {
        when(sucursalRepository.findAll()).thenReturn(List.of(sucursal));
        assertEquals(1, sucursalService.listarTodas().size());
    }

    @Test
    void testListarActivas() {
        when(sucursalRepository.findByActivaTrue()).thenReturn(List.of(sucursal));
        assertFalse(sucursalService.listarActivas().isEmpty());
    }

    @Test
    void testObtenerPorId_Existente() {
        when(sucursalRepository.findById(1L)).thenReturn(Optional.of(sucursal));
        assertEquals("Sucursal Centro", sucursalService.obtenerPorId(1L).getNombre());
    }

    @Test
    void testObtenerPorId_NoExistente_LanzaExcepcion() {
        when(sucursalRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> sucursalService.obtenerPorId(99L));
    }

    @Test
    void testCrear() {
        when(sucursalRepository.save(sucursal)).thenReturn(sucursal);
        assertNotNull(sucursalService.crear(sucursal));
    }

    @Test
    void testActualizar() {
        Sucursal datosNuevos = new Sucursal();
        datosNuevos.setNombre("Sucursal Renovada");
        datosNuevos.setDireccion("Nueva dirección 456");
        datosNuevos.setComuna("Ñuñoa");
        datosNuevos.setTelefono("+56911112222");
        datosNuevos.setActiva(true);

        when(sucursalRepository.findById(1L)).thenReturn(Optional.of(sucursal));
        when(sucursalRepository.save(any(Sucursal.class))).thenAnswer(inv -> inv.getArgument(0));

        Sucursal actualizada = sucursalService.actualizar(1L, datosNuevos);

        assertEquals("Sucursal Renovada", actualizada.getNombre());
        assertEquals("Ñuñoa", actualizada.getComuna());
    }

    @Test
    void testEliminar_HaceBajaLogica() {
        when(sucursalRepository.findById(1L)).thenReturn(Optional.of(sucursal));
        when(sucursalRepository.save(any(Sucursal.class))).thenAnswer(inv -> inv.getArgument(0));

        sucursalService.eliminar(1L);

        assertFalse(sucursal.getActiva());
        verify(sucursalRepository, times(1)).save(sucursal);
    }

    // ---------------------------------------------------------
    // Stock por sucursal
    // ---------------------------------------------------------

    @Test
    void testListarStockPorSucursal() {
        when(sucursalRepository.findById(1L)).thenReturn(Optional.of(sucursal));
        when(stockSucursalRepository.findBySucursalId(1L)).thenReturn(List.of(new StockSucursal()));

        assertEquals(1, sucursalService.listarStockPorSucursal(1L).size());
    }

    @Test
    void testAsignarStock_NuevoRegistro() {
        when(sucursalRepository.findById(1L)).thenReturn(Optional.of(sucursal));
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(stockSucursalRepository.findBySucursalIdAndProductoId(1L, 10L)).thenReturn(Optional.empty());
        when(stockSucursalRepository.save(any(StockSucursal.class))).thenAnswer(inv -> inv.getArgument(0));

        StockSucursal resultado = sucursalService.asignarStock(1L, 10L, 40, 10);

        assertEquals(40, resultado.getCantidad());
        assertEquals(10, resultado.getStockMinimo());
        assertEquals(sucursal, resultado.getSucursal());
        assertEquals(producto, resultado.getProducto());
    }

    @Test
    void testAsignarStock_ProductoInexistente_LanzaExcepcion() {
        when(sucursalRepository.findById(1L)).thenReturn(Optional.of(sucursal));
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> sucursalService.asignarStock(1L, 99L, 10, 5));
    }

    @Test
    void testAjustarStock_Incremento_NoGatillaReposicion() {
        StockSucursal stock = new StockSucursal();
        stock.setSucursal(sucursal);
        stock.setProducto(producto);
        stock.setCantidad(20);
        stock.setStockMinimo(10);

        when(stockSucursalRepository.findBySucursalIdAndProductoId(1L, 10L)).thenReturn(Optional.of(stock));
        when(stockSucursalRepository.save(any(StockSucursal.class))).thenAnswer(inv -> inv.getArgument(0));

        StockSucursal resultado = sucursalService.ajustarStock(1L, 10L, 15);

        assertEquals(35, resultado.getCantidad());
        verify(ordenCompraService, never()).generarOrdenAutomatica(any());
    }

    @Test
    void testAjustarStock_BajoMinimo_GatillaReposicionAutomatica() {
        StockSucursal stock = new StockSucursal();
        stock.setSucursal(sucursal);
        stock.setProducto(producto);
        stock.setCantidad(15);
        stock.setStockMinimo(10);

        when(stockSucursalRepository.findBySucursalIdAndProductoId(1L, 10L)).thenReturn(Optional.of(stock));
        when(stockSucursalRepository.save(any(StockSucursal.class))).thenAnswer(inv -> inv.getArgument(0));

        // 15 - 8 = 7, queda por debajo del mínimo (10) -> debe gatillar la orden automática
        StockSucursal resultado = sucursalService.ajustarStock(1L, 10L, -8);

        assertEquals(7, resultado.getCantidad());
        verify(ordenCompraService, times(1)).generarOrdenAutomatica(resultado);
    }

    @Test
    void testAjustarStock_DejaStockNegativo_LanzaExcepcion() {
        StockSucursal stock = new StockSucursal();
        stock.setSucursal(sucursal);
        stock.setProducto(producto);
        stock.setCantidad(5);
        stock.setStockMinimo(10);

        when(stockSucursalRepository.findBySucursalIdAndProductoId(1L, 10L)).thenReturn(Optional.of(stock));

        assertThrows(InsufficientStockException.class,
                () -> sucursalService.ajustarStock(1L, 10L, -20));
        verify(stockSucursalRepository, never()).save(any());
    }

    @Test
    void testAjustarStock_SinRegistroPrevio_LanzaExcepcion() {
        when(stockSucursalRepository.findBySucursalIdAndProductoId(1L, 10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> sucursalService.ajustarStock(1L, 10L, -5));
    }

    // ---------------------------------------------------------
    // Disponibilidad
    // ---------------------------------------------------------

    @Test
    void testConsultarDisponibilidad_SoloSucursalesActivas() {
        Sucursal sucursalInactiva = new Sucursal();
        sucursalInactiva.setId(2L);
        sucursalInactiva.setNombre("Sucursal Cerrada");
        sucursalInactiva.setActiva(false);

        StockSucursal stockActivo = new StockSucursal();
        stockActivo.setSucursal(sucursal);
        stockActivo.setProducto(producto);
        stockActivo.setCantidad(12);

        StockSucursal stockInactivo = new StockSucursal();
        stockInactivo.setSucursal(sucursalInactiva);
        stockInactivo.setProducto(producto);
        stockInactivo.setCantidad(5);

        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(stockSucursalRepository.findByProductoId(10L)).thenReturn(List.of(stockActivo, stockInactivo));

        List<DisponibilidadDTO> disponibilidad = sucursalService.consultarDisponibilidad(10L);

        assertEquals(1, disponibilidad.size());
        assertEquals("Sucursal Centro", disponibilidad.get(0).getSucursalNombre());
        assertTrue(disponibilidad.get(0).getDisponible());
    }

    @Test
    void testConsultarDisponibilidad_ProductoInexistente_LanzaExcepcion() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> sucursalService.consultarDisponibilidad(99L));
    }
}
