package com.minimarket;

import com.minimarket.entity.*;
import com.minimarket.exception.ResourceNotFoundException;
import com.minimarket.repository.OrdenCompraRepository;
import com.minimarket.repository.ProveedorRepository;
import com.minimarket.repository.StockSucursalRepository;
import com.minimarket.service.impl.OrdenCompraServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrdenCompraServiceTest {

    @Mock
    private OrdenCompraRepository ordenCompraRepository;

    @Mock
    private ProveedorRepository proveedorRepository;

    @Mock
    private StockSucursalRepository stockSucursalRepository;

    @InjectMocks
    private OrdenCompraServiceImpl ordenCompraService;

    private Sucursal sucursal;
    private Producto producto;
    private Proveedor proveedor;
    private StockSucursal stockBajoMinimo;

    @BeforeEach
    void setUp() {
        sucursal = new Sucursal();
        sucursal.setId(1L);
        sucursal.setNombre("Sucursal Centro");

        producto = new Producto();
        producto.setId(10L);
        producto.setNombre("Leche Entera 1L");

        proveedor = new Proveedor();
        proveedor.setId(5L);
        proveedor.setNombre("Distribuidora Andina SpA");
        proveedor.setActivo(true);

        stockBajoMinimo = new StockSucursal();
        stockBajoMinimo.setSucursal(sucursal);
        stockBajoMinimo.setProducto(producto);
        stockBajoMinimo.setCantidad(3);
        stockBajoMinimo.setStockMinimo(10);
    }

    @Test
    void testListarTodas() {
        when(ordenCompraRepository.findAll()).thenReturn(List.of(new OrdenCompra()));
        assertEquals(1, ordenCompraService.listarTodas().size());
    }

    @Test
    void testListarPorEstado() {
        when(ordenCompraRepository.findByEstado(EstadoOrdenCompra.PENDIENTE)).thenReturn(List.of(new OrdenCompra()));
        assertFalse(ordenCompraService.listarPorEstado(EstadoOrdenCompra.PENDIENTE).isEmpty());
    }

    @Test
    void testObtenerPorId_Existente() {
        OrdenCompra orden = new OrdenCompra();
        orden.setId(1L);
        when(ordenCompraRepository.findById(1L)).thenReturn(Optional.of(orden));
        assertEquals(1L, ordenCompraService.obtenerPorId(1L).getId());
    }

    @Test
    void testObtenerPorId_NoExistente_LanzaExcepcion() {
        when(ordenCompraRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> ordenCompraService.obtenerPorId(99L));
    }

    @Test
    void testCambiarEstado_ARecibida_IngresaStockALaSucursal() {
        OrdenCompra orden = new OrdenCompra();
        orden.setId(1L);
        orden.setProducto(producto);
        orden.setSucursal(sucursal);
        orden.setCantidadSolicitada(30);
        orden.setEstado(EstadoOrdenCompra.PENDIENTE);

        StockSucursal stockExistente = new StockSucursal();
        stockExistente.setSucursal(sucursal);
        stockExistente.setProducto(producto);
        stockExistente.setCantidad(3);
        stockExistente.setStockMinimo(10);

        when(ordenCompraRepository.findById(1L)).thenReturn(Optional.of(orden));
        when(ordenCompraRepository.save(any(OrdenCompra.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stockSucursalRepository.findBySucursalIdAndProductoId(1L, 10L)).thenReturn(Optional.of(stockExistente));

        OrdenCompra resultado = ordenCompraService.cambiarEstado(1L, EstadoOrdenCompra.RECIBIDA);

        assertEquals(EstadoOrdenCompra.RECIBIDA, resultado.getEstado());
        assertEquals(33, stockExistente.getCantidad());
        verify(stockSucursalRepository, times(1)).save(stockExistente);
    }

    @Test
    void testCambiarEstado_ACancelada_NoTocaStock() {
        OrdenCompra orden = new OrdenCompra();
        orden.setId(1L);
        orden.setProducto(producto);
        orden.setSucursal(sucursal);
        orden.setEstado(EstadoOrdenCompra.PENDIENTE);

        when(ordenCompraRepository.findById(1L)).thenReturn(Optional.of(orden));
        when(ordenCompraRepository.save(any(OrdenCompra.class))).thenAnswer(inv -> inv.getArgument(0));

        OrdenCompra resultado = ordenCompraService.cambiarEstado(1L, EstadoOrdenCompra.CANCELADA);

        assertEquals(EstadoOrdenCompra.CANCELADA, resultado.getEstado());
        verify(stockSucursalRepository, never()).save(any());
    }

    @Test
    void testGenerarOrdenAutomatica_CreaOrdenConProveedorActivo() {
        when(ordenCompraRepository.existsByProductoIdAndSucursalIdAndEstado(
                10L, 1L, EstadoOrdenCompra.PENDIENTE)).thenReturn(false);
        when(proveedorRepository.findByActivoTrue()).thenReturn(List.of(proveedor));

        ordenCompraService.generarOrdenAutomatica(stockBajoMinimo);

        ArgumentCaptor<OrdenCompra> captor = ArgumentCaptor.forClass(OrdenCompra.class);
        verify(ordenCompraRepository, times(1)).save(captor.capture());

        OrdenCompra creada = captor.getValue();
        assertEquals(proveedor, creada.getProveedor());
        assertEquals(producto, creada.getProducto());
        assertEquals(sucursal, creada.getSucursal());
        assertTrue(creada.getAutomatica());
        // stockMinimo(10) * FACTOR(3) - cantidad(3) = 27
        assertEquals(27, creada.getCantidadSolicitada());
    }

    @Test
    void testGenerarOrdenAutomatica_YaExistePendiente_NoDuplica() {
        when(ordenCompraRepository.existsByProductoIdAndSucursalIdAndEstado(
                10L, 1L, EstadoOrdenCompra.PENDIENTE)).thenReturn(true);

        ordenCompraService.generarOrdenAutomatica(stockBajoMinimo);

        verify(ordenCompraRepository, never()).save(any());
        verifyNoInteractions(proveedorRepository);
    }

    @Test
    void testGenerarOrdenAutomatica_SinProveedoresActivos_NoCreaOrden() {
        when(ordenCompraRepository.existsByProductoIdAndSucursalIdAndEstado(
                10L, 1L, EstadoOrdenCompra.PENDIENTE)).thenReturn(false);
        when(proveedorRepository.findByActivoTrue()).thenReturn(List.of());

        ordenCompraService.generarOrdenAutomatica(stockBajoMinimo);

        verify(ordenCompraRepository, never()).save(any());
    }
}
