package com.minimarket;

import com.minimarket.entity.DetalleVenta;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Usuario;
import com.minimarket.entity.Venta;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.VentaRepository;
import com.minimarket.service.impl.VentaServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VentaServiceTest {

    @Mock
    private VentaRepository ventaRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private VentaServiceImpl ventaService;

    private Venta venta;
    private Producto producto1;
    private Producto producto2;
    private DetalleVenta detalle1;
    private DetalleVenta detalle2;
    private Usuario usuario;

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

        producto1 = new Producto();
        producto1.setId(1L);
        producto1.setNombre("Leche");
        producto1.setPrecio(1200.0);
        producto1.setStock(10);

        producto2 = new Producto();
        producto2.setId(2L);
        producto2.setNombre("Pan");
        producto2.setPrecio(800.0);
        producto2.setStock(5);

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
        venta.setFecha(new Date());
        venta.setDetalles(Arrays.asList(detalle1, detalle2));

        detalle1.setVenta(venta);
        detalle2.setVenta(venta);
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

    // --- Pruebas Reales de Lógica de Negocio (Service) ---

    @Test
    void testGuardarVentaSinUsuarioLanzaExcepcion() {
        venta.setUsuario(null); // Simulamos una venta sin usuario

        Exception exception = assertThrows(RuntimeException.class, () -> {
            ventaService.save(venta);
        });

        assertEquals("La venta debe estar vinculada a un usuario válido", exception.getMessage());
        verify(ventaRepository, never()).save(any(Venta.class)); // Garantiza que no se guardó
    }

    @Test
    void testGuardarVentaConStockInsuficienteLanzaExcepcion() {
        // Arrange: Forzamos que el producto1 tenga menos stock del que se pide (1 vs 2)
        producto1.setStock(1);

        // Cuando el servicio busque el producto, devolvemos el producto con stock insuficiente
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto1));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            ventaService.save(venta);
        });

        assertTrue(exception.getMessage().contains("Stock insuficiente para el producto: Leche"));
        // Verificamos que NUNCA se guardó la venta
        verify(ventaRepository, never()).save(any(Venta.class));
    }

    @Test
    void testGuardarVentaConStockSuficienteEsExitoso() {
        // Arrange: Productos con stock suficiente
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto1));
        when(productoRepository.findById(2L)).thenReturn(Optional.of(producto2));
        when(ventaRepository.save(any(Venta.class))).thenReturn(venta);

        // Act
        Venta guardada = ventaService.save(venta);

        // Assert
        assertNotNull(guardada);
        // Verificamos que se descontó el stock correctamente
        assertEquals(8, producto1.getStock()); // Leche: 10 inicial - 2 = 8
        assertEquals(2, producto2.getStock()); // Pan: 5 inicial - 3 = 2

        verify(ventaRepository, times(1)).save(venta);
        // Verificar que los productos se guardaron con su nuevo stock
        verify(productoRepository, times(1)).save(producto1);
        verify(productoRepository, times(1)).save(producto2);

        // Aserciones más finas sobre los datos retornados: precios sincronizados
        // con el catálogo y estado final coherente con lo esperado.
        assertEquals(1200.0, guardada.getDetalles().get(0).getPrecio());
        assertEquals(800.0, guardada.getDetalles().get(1).getPrecio());
    }

    // --- Pruebas de cálculo de total (sugerencia del docente) ---

    @Test
    void testCalculoDeTotalDeVentaEsCorrecto() {
        // Arrange: Leche 2 x 1200 = 2400; Pan 3 x 800 = 2400; total esperado = 4800
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto1));
        when(productoRepository.findById(2L)).thenReturn(Optional.of(producto2));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Venta guardada = ventaService.save(venta);

        // Assert: aserción directa sobre la suma esperada de precios, no solo sobre la ejecución
        assertNotNull(guardada.getTotal(), "La venta guardada debe exponer un total calculado");
        assertEquals(4800.0, guardada.getTotal(), 0.001);
    }

    @Test
    void testCalculoDeTotalConPreciosDecimalesRedondeaCorrectamente() {
        // Arrange: precios con decimales que podrían generar errores de redondeo con Double puro
        producto1.setPrecio(1099.99);
        producto1.setStock(10);
        detalle1.setCantidad(3); // 1099.99 * 3 = 3299.97

        producto2.setPrecio(0.1);
        producto2.setStock(10);
        detalle2.setCantidad(3); // 0.1 * 3 = 0.30 (caso clásico de imprecisión binaria)

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto1));
        when(productoRepository.findById(2L)).thenReturn(Optional.of(producto2));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Venta guardada = ventaService.save(venta);

        // Assert: el total debe quedar redondeado a 2 decimales de forma exacta (3300.27)
        assertEquals(3300.27, guardada.getTotal(), 0.001);
    }

    // --- Validación defensiva de detalles nulos o vacíos (sugerencia del docente) ---

    @Test
    void testGuardarVentaConListaDeDetallesVaciaLanzaExcepcion() {
        venta.setDetalles(new ArrayList<>());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            ventaService.save(venta);
        });

        assertEquals("La venta debe contener al menos un detalle", exception.getMessage());
        verify(ventaRepository, never()).save(any(Venta.class));
    }

    @Test
    void testGuardarVentaConListaDeDetallesNulaLanzaExcepcion() {
        venta.setDetalles(null);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            ventaService.save(venta);
        });

        assertEquals("La venta debe contener al menos un detalle", exception.getMessage());
        verify(ventaRepository, never()).save(any(Venta.class));
    }

    @Test
    void testGuardarVentaConDetalleSinProductoLanzaExcepcion() {
        DetalleVenta detalleInvalido = new DetalleVenta();
        detalleInvalido.setCantidad(1);
        detalleInvalido.setProducto(null);
        venta.setDetalles(Collections.singletonList(detalleInvalido));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            ventaService.save(venta);
        });

        assertEquals("El detalle de venta no tiene un producto válido", exception.getMessage());
        verify(ventaRepository, never()).save(any(Venta.class));
    }

    // --- Simulación de concurrencia en la reducción de stock (sugerencia del docente) ---

    @Test
    void testGuardarVentaConFalloDeConcurrenciaEnStockLanzaExcepcionDeNegocio() {
        // Arrange: simulamos que otra transacción modificó el producto al mismo tiempo,
        // provocando que Spring Data JPA lance OptimisticLockingFailureException
        // al intentar persistir el descuento de stock (gracias a @Version en Producto).
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto1));
        when(productoRepository.save(producto1))
                .thenThrow(new OptimisticLockingFailureException("Conflicto de versión simulado"));

        venta.setDetalles(Collections.singletonList(detalle1));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            ventaService.save(venta);
        });

        assertTrue(exception.getMessage().contains("modificado por otra operación simultánea"));
        // La venta nunca debe guardarse si el descuento de stock falla por concurrencia
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
    void testGuardarVentaConUsuarioSinIdLanzaExcepcion() {
        // La venta tiene usuario, pero su ID es null
        usuario.setId(null);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            ventaService.save(venta);
        });

        assertEquals("La venta debe estar vinculada a un usuario válido", exception.getMessage());
    }

    @Test
    void testGuardarVentaConProductoNoEncontradoLanzaExcepcion() {
        // Simulamos que al ir a buscar el producto a la BD, no existe
        when(productoRepository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            ventaService.save(venta);
        });

        assertEquals("Producto no encontrado", exception.getMessage());
    }

    // --- Aserción adicional sobre el objeto efectivamente persistido ---

    @Test
    void testVentaPersistidaIncluyeTotalYDetallesVinculados() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto1));
        when(productoRepository.findById(2L)).thenReturn(Optional.of(producto2));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<Venta> ventaCaptor = ArgumentCaptor.forClass(Venta.class);

        ventaService.save(venta);

        verify(ventaRepository).save(ventaCaptor.capture());
        Venta ventaPersistida = ventaCaptor.getValue();

        assertEquals(4800.0, ventaPersistida.getTotal(), 0.001);
        assertEquals(venta, ventaPersistida.getDetalles().get(0).getVenta());
        assertEquals(venta, ventaPersistida.getDetalles().get(1).getVenta());
    }
}