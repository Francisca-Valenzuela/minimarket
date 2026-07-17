package com.minimarket;

import com.minimarket.entity.DetalleVenta;
import com.minimarket.entity.Producto;
import com.minimarket.entity.StockSucursal;
import com.minimarket.entity.Sucursal;
import com.minimarket.entity.TipoEntrega;
import com.minimarket.entity.Usuario;
import com.minimarket.entity.Venta;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.PromocionRepository;
import com.minimarket.repository.StockSucursalRepository;
import com.minimarket.repository.SucursalRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.repository.VentaRepository;
import com.minimarket.service.OrdenCompraService;
import com.minimarket.service.impl.VentaServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VentaServiceTest {

    @Mock private VentaRepository ventaRepository;
    @Mock private ProductoRepository productoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private SucursalRepository sucursalRepository;
    @Mock private StockSucursalRepository stockSucursalRepository;
    @Mock private PromocionRepository promocionRepository;
    @Mock private OrdenCompraService ordenCompraService;

    private VentaServiceImpl ventaService;

    private Venta venta;
    private Usuario usuario;
    private Sucursal sucursal;
    private Producto producto1;
    private Producto producto2;
    private DetalleVenta detalle1;
    private DetalleVenta detalle2;
    private StockSucursal stockSucursal1;
    private StockSucursal stockSucursal2;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("vendedor");
        usuario.setPassword("pass");
        usuario.setNombre("Juan");
        usuario.setApellido("Perez");
        usuario.setEmail("juan@test.com");
        usuario.setDireccion("Avenida 123");

        sucursal = new Sucursal();
        sucursal.setId(1L);

        producto1 = new Producto();
        producto1.setId(1L);
        producto1.setNombre("Leche");
        producto1.setPrecio(1200.0);

        producto2 = new Producto();
        producto2.setId(2L);
        producto2.setNombre("Pan");
        producto2.setPrecio(800.0);

        // El stock ya no vive en Producto: vive en StockSucursal (por sucursal_id + producto_id)
        stockSucursal1 = new StockSucursal();
        stockSucursal1.setId(1L);
        stockSucursal1.setSucursal(sucursal);
        stockSucursal1.setProducto(producto1);
        stockSucursal1.setCantidad(10);

        stockSucursal2 = new StockSucursal();
        stockSucursal2.setId(2L);
        stockSucursal2.setSucursal(sucursal);
        stockSucursal2.setProducto(producto2);
        stockSucursal2.setCantidad(5);

        detalle1 = new DetalleVenta();
        detalle1.setProducto(producto1);
        detalle1.setCantidad(2);
        detalle1.setPrecio(producto1.getPrecio());

        detalle2 = new DetalleVenta();
        detalle2.setProducto(producto2);
        detalle2.setCantidad(3);
        detalle2.setPrecio(producto2.getPrecio());

        venta = new Venta();
        venta.setId(1L);
        venta.setUsuario(usuario);
        venta.setSucursal(sucursal);
        venta.setTipoEntrega(TipoEntrega.RETIRO_TIENDA);
        venta.setFecha(new Date());
        venta.setDetalles(Arrays.asList(detalle1, detalle2));

        detalle1.setVenta(venta);
        detalle2.setVenta(venta);

        // Stubs comunes (lenient: no todos los tests los necesitan, y no queremos
        // que Mockito marque "unnecessary stubbing" en los que fallan antes de llegar aquí)
        lenient().when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        lenient().when(sucursalRepository.findById(1L)).thenReturn(Optional.of(sucursal));
        lenient().when(promocionRepository.buscarActivasParaProductoYSucursal(anyLong(), anyLong()))
                .thenReturn(List.of());

        // Se instancia manualmente (mismo criterio que en CarritoControllerTest) para
        // evitar depender de la resolución de constructor de @InjectMocks ahora que
        // VentaServiceImpl tiene 7 dependencias, incluyendo OrdenCompraService.
        ventaService = new VentaServiceImpl(ventaRepository, productoRepository, promocionRepository,
                usuarioRepository, sucursalRepository, stockSucursalRepository, ordenCompraService);
    }

    // --- Pruebas Estructurales ---

    @Test
    void testVentaTieneUsuarioAsociado() {
        assertNotNull(venta.getUsuario(), "La venta debe tener un usuario asociado");
        assertEquals("vendedor", venta.getUsuario().getUsername());
    }

    @Test
    void testVentaTieneFechaAsignada() {
        assertNotNull(venta.getFecha(), "La venta debe tener una fecha");
    }

    @Test
    void testVentaTieneDetalles() {
        assertNotNull(venta.getDetalles());
        assertFalse(venta.getDetalles().isEmpty(), "La venta debe tener detalles");
    }

    @Test
    void testDetalleVentaEstaAsociadoAVenta() {
        DetalleVenta detalle = venta.getDetalles().get(0);
        assertNotNull(detalle.getVenta(), "El detalle debe estar asociado a una venta");
        assertEquals(venta.getId(), detalle.getVenta().getId());
    }

    @Test
    void testDetalleVentaTieneProductoAsociado() {
        DetalleVenta detalle = venta.getDetalles().get(0);
        assertNotNull(detalle.getProducto(), "El detalle debe tener un producto");
        assertNotNull(detalle.getProducto().getNombre());
    }

    // --- Validaciones de usuario ---

    @Test
    void testGuardarVentaSinUsuarioLanzaExcepcion() {
        venta.setUsuario(null);

        Exception exception = assertThrows(RuntimeException.class, () -> ventaService.save(venta));

        assertEquals("La venta debe estar vinculada a un usuario válido", exception.getMessage());
        verify(ventaRepository, never()).save(any(Venta.class));
    }

    @Test
    void testGuardarVentaConUsuarioSinIdLanzaExcepcion() {
        usuario.setId(null);

        Exception exception = assertThrows(RuntimeException.class, () -> ventaService.save(venta));

        assertEquals("La venta debe estar vinculada a un usuario válido", exception.getMessage());
    }

    // --- Validaciones de stock por sucursal ---

    @Test
    void testGuardarVentaConStockInsuficienteLanzaExcepcion() {
        stockSucursal1.setCantidad(1); // menos que los 2 solicitados en detalle1
        venta.setDetalles(List.of(detalle1));

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto1));
        when(stockSucursalRepository.findBySucursalIdAndProductoId(1L, 1L))
                .thenReturn(Optional.of(stockSucursal1));

        Exception exception = assertThrows(RuntimeException.class, () -> ventaService.save(venta));

        assertTrue(exception.getMessage().contains("Stock insuficiente para el producto 'Leche'"));
        verify(ventaRepository, never()).save(any(Venta.class));
    }

    @Test
    void testGuardarVentaConStockSuficienteEsExitoso() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto1));
        when(productoRepository.findById(2L)).thenReturn(Optional.of(producto2));
        when(stockSucursalRepository.findBySucursalIdAndProductoId(1L, 1L))
                .thenReturn(Optional.of(stockSucursal1));
        when(stockSucursalRepository.findBySucursalIdAndProductoId(1L, 2L))
                .thenReturn(Optional.of(stockSucursal2));
        when(ventaRepository.save(any(Venta.class))).thenReturn(venta);

        Venta guardada = ventaService.save(venta);

        assertNotNull(guardada);
        // El descuento ahora ocurre en StockSucursal, no en Producto
        assertEquals(8, stockSucursal1.getCantidad()); // 10 - 2
        assertEquals(2, stockSucursal2.getCantidad()); // 5 - 3

        verify(ventaRepository, times(1)).save(venta);
        verify(stockSucursalRepository, times(1)).save(stockSucursal1);
        verify(stockSucursalRepository, times(1)).save(stockSucursal2);

        assertEquals(1200.0, guardada.getDetalles().get(0).getPrecio());
        assertEquals(800.0, guardada.getDetalles().get(1).getPrecio());
    }

    @Test
    void testGuardarVenta_DejaStockBajoElMinimo_DisparaReposicionAutomatica() {
        stockSucursal1.setStockMinimo(10); // 10 - 2 = 8, queda <= mínimo
        stockSucursal2.setStockMinimo(1);  // 5 - 3 = 2, NO queda bajo el mínimo

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto1));
        when(productoRepository.findById(2L)).thenReturn(Optional.of(producto2));
        when(stockSucursalRepository.findBySucursalIdAndProductoId(1L, 1L))
                .thenReturn(Optional.of(stockSucursal1));
        when(stockSucursalRepository.findBySucursalIdAndProductoId(1L, 2L))
                .thenReturn(Optional.of(stockSucursal2));
        when(ventaRepository.save(any(Venta.class))).thenReturn(venta);

        ventaService.save(venta);

        verify(ordenCompraService, times(1)).generarOrdenAutomatica(stockSucursal1);
        verify(ordenCompraService, never()).generarOrdenAutomatica(stockSucursal2);
    }

    @Test
    void testGuardarVenta_StockSinMinimoDefinido_NoDisparaReposicion() {
        // stockMinimo queda null (valor por defecto del setUp) -> no debe intentar comparar ni disparar nada
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto1));
        when(productoRepository.findById(2L)).thenReturn(Optional.of(producto2));
        when(stockSucursalRepository.findBySucursalIdAndProductoId(1L, 1L))
                .thenReturn(Optional.of(stockSucursal1));
        when(stockSucursalRepository.findBySucursalIdAndProductoId(1L, 2L))
                .thenReturn(Optional.of(stockSucursal2));
        when(ventaRepository.save(any(Venta.class))).thenReturn(venta);

        ventaService.save(venta);

        verifyNoInteractions(ordenCompraService);
    }

    // --- Cálculo de total ---

    @Test
    void testCalculoDeTotalDeVentaEsCorrecto() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto1));
        when(productoRepository.findById(2L)).thenReturn(Optional.of(producto2));
        when(stockSucursalRepository.findBySucursalIdAndProductoId(1L, 1L))
                .thenReturn(Optional.of(stockSucursal1));
        when(stockSucursalRepository.findBySucursalIdAndProductoId(1L, 2L))
                .thenReturn(Optional.of(stockSucursal2));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> inv.getArgument(0));

        Venta guardada = ventaService.save(venta);

        assertNotNull(guardada.getTotal(), "La venta guardada debe exponer un total calculado");
        assertEquals(4800.0, guardada.getTotal(), 0.001);
    }

    @Test
    void testCalculoDeTotalConPreciosDecimalesRedondeaCorrectamente() {
        producto1.setPrecio(1099.99);
        detalle1.setCantidad(3); // 1099.99 * 3 = 3299.97

        producto2.setPrecio(0.1);
        detalle2.setCantidad(3); // 0.1 * 3 = 0.30 (imprecisión binaria clásica)

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto1));
        when(productoRepository.findById(2L)).thenReturn(Optional.of(producto2));
        when(stockSucursalRepository.findBySucursalIdAndProductoId(1L, 1L))
                .thenReturn(Optional.of(stockSucursal1));
        when(stockSucursalRepository.findBySucursalIdAndProductoId(1L, 2L))
                .thenReturn(Optional.of(stockSucursal2));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> inv.getArgument(0));

        Venta guardada = ventaService.save(venta);

        assertEquals(3300.27, guardada.getTotal(), 0.001);
    }

    // --- Validación defensiva de detalles nulos o vacíos ---

    @Test
    void testGuardarVentaConListaDeDetallesVaciaLanzaExcepcion() {
        venta.setDetalles(new ArrayList<>());

        Exception exception = assertThrows(RuntimeException.class, () -> ventaService.save(venta));

        assertEquals("La venta debe contener al menos un detalle", exception.getMessage());
        verify(ventaRepository, never()).save(any(Venta.class));
    }

    @Test
    void testGuardarVentaConListaDeDetallesNulaLanzaExcepcion() {
        venta.setDetalles(null);

        Exception exception = assertThrows(RuntimeException.class, () -> ventaService.save(venta));

        assertEquals("La venta debe contener al menos un detalle", exception.getMessage());
        verify(ventaRepository, never()).save(any(Venta.class));
    }

    @Test
    void testGuardarVentaConDetalleSinProductoLanzaExcepcion() {
        DetalleVenta detalleInvalido = new DetalleVenta();
        detalleInvalido.setCantidad(1);
        detalleInvalido.setProducto(null);
        venta.setDetalles(Collections.singletonList(detalleInvalido));

        Exception exception = assertThrows(RuntimeException.class, () -> ventaService.save(venta));

        assertEquals("El detalle de venta no tiene un producto válido", exception.getMessage());
        verify(ventaRepository, never()).save(any(Venta.class));
    }

    // --- Concurrencia (ahora sobre StockSucursal, no sobre Producto) ---

    @Test
    void testGuardarVentaConFalloDeConcurrenciaEnStockLanzaExcepcionDeNegocio() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto1));
        when(stockSucursalRepository.findBySucursalIdAndProductoId(1L, 1L))
                .thenReturn(Optional.of(stockSucursal1));
        when(stockSucursalRepository.save(stockSucursal1))
                .thenThrow(new OptimisticLockingFailureException("Conflicto de versión simulado"));

        venta.setDetalles(Collections.singletonList(detalle1));

        Exception exception = assertThrows(RuntimeException.class, () -> ventaService.save(venta));

        assertTrue(exception.getMessage().contains("modificado por otra operación simultánea"));
        verify(ventaRepository, never()).save(any(Venta.class));
    }

    // --- Pruebas paramétricas de límites de stock (ahora sobre StockSucurs
    // --- Pruebas paramétricas de límites de stock (ahora sobre StockSucursal) ---

    @ParameterizedTest(name = "stock={0}, cantidadVendida={1} -> debe permitir la venta")
    @CsvSource({
        "10, 10", // límite exacto
        "10, 1",
        "1, 1"
    })
    void testGuardarVenta_conCantidadEnElLimiteDeStock_esExitosa(int stock, int cantidadVendida) {
        stockSucursal1.setCantidad(stock);
        detalle1.setCantidad(cantidadVendida);
        venta.setDetalles(Collections.singletonList(detalle1));

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto1));
        when(stockSucursalRepository.findBySucursalIdAndProductoId(1L, 1L))
                .thenReturn(Optional.of(stockSucursal1));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> inv.getArgument(0));

        Venta guardada = ventaService.save(venta);

        assertNotNull(guardada);
        assertEquals(stock - cantidadVendida, stockSucursal1.getCantidad());
        verify(ventaRepository, times(1)).save(venta);
    }

    @ParameterizedTest(name = "stock={0}, cantidadVendida={1} -> debe rechazar por stock insuficiente")
    @CsvSource({
        "10, 11",
        "1, 2",
        "0, 1"
    })
    void testGuardarVenta_conCantidadSobreElLimiteDeStock_lanzaExcepcion(int stock, int cantidadVendida) {
        stockSucursal1.setCantidad(stock);
        detalle1.setCantidad(cantidadVendida);
        venta.setDetalles(Collections.singletonList(detalle1));

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto1));
        when(stockSucursalRepository.findBySucursalIdAndProductoId(1L, 1L))
                .thenReturn(Optional.of(stockSucursal1));

        assertThrows(RuntimeException.class, () -> ventaService.save(venta));
        verify(ventaRepository, never()).save(any(Venta.class));
    }

    // --- Producto sin registro de stock en la sucursal (escenario 3 validado por Postman) ---

    @Test
    void testGuardarVentaConProductoSinStockEnSucursalLanzaExcepcion() {
        venta.setDetalles(Collections.singletonList(detalle1));

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto1));
        when(stockSucursalRepository.findBySucursalIdAndProductoId(1L, 1L))
                .thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> ventaService.save(venta));

        assertTrue(exception.getMessage().contains("no tiene stock registrado en esta sucursal"));
        verify(ventaRepository, never()).save(any(Venta.class));
    }

    // --- Mock de VentaRepository (Lecturas) ---

    @Test
    void testBuscarVentaPorIdExistente() {
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(venta));

        Venta encontrada = ventaService.findById(1L);

        assertNotNull(encontrada);
        assertEquals(1L, encontrada.getId());
    }

    @Test
    void testBuscarVentasPorUsuario() {
        when(ventaRepository.findByUsuarioId(1L)).thenReturn(List.of(venta));

        List<Venta> ventas = ventaService.findByUsuarioId(1L);

        assertFalse(ventas.isEmpty());
        assertEquals(1, ventas.size());
        verify(ventaRepository, times(1)).findByUsuarioId(1L);
    }

    @Test
    void testFindAllVentas() {
        when(ventaRepository.findAll()).thenReturn(List.of(venta));

        List<Venta> lista = ventaService.findAll();

        assertFalse(lista.isEmpty(), "La lista de ventas no debe estar vacía");
        verify(ventaRepository, times(1)).findAll();
    }

    @Test
    void testBuscarVentaPorIdNoExistenteRetornaNull() {
        when(ventaRepository.findById(99L)).thenReturn(Optional.empty());

        Venta encontrada = ventaService.findById(99L);

        assertNull(encontrada, "Si el ID no existe, debe retornar null");
    }

    @Test
    void testGuardarVentaConProductoNoEncontradoLanzaExcepcion() {
        when(productoRepository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> ventaService.save(venta));

        assertEquals("Producto no encontrado", exception.getMessage());
    }

    // --- Aserción adicional sobre el objeto efectivamente persistido ---

    @Test
    void testVentaPersistidaIncluyeTotalYDetallesVinculados() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto1));
        when(productoRepository.findById(2L)).thenReturn(Optional.of(producto2));
        when(stockSucursalRepository.findBySucursalIdAndProductoId(1L, 1L))
                .thenReturn(Optional.of(stockSucursal1));
        when(stockSucursalRepository.findBySucursalIdAndProductoId(1L, 2L))
                .thenReturn(Optional.of(stockSucursal2));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<Venta> ventaCaptor = ArgumentCaptor.forClass(Venta.class);

        ventaService.save(venta);

        verify(ventaRepository).save(ventaCaptor.capture());
        Venta ventaPersistida = ventaCaptor.getValue();

        assertEquals(4800.0, ventaPersistida.getTotal(), 0.001);
        assertEquals(venta, ventaPersistida.getDetalles().get(0).getVenta());
        assertEquals(venta, ventaPersistida.getDetalles().get(1).getVenta());
    }
}

