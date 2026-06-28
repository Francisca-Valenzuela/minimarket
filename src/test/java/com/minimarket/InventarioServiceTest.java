package com.minimarket;

import com.minimarket.entity.Inventario;
import com.minimarket.entity.Producto;
import com.minimarket.repository.InventarioRepository;
import com.minimarket.service.impl.InventarioServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventarioServiceTest {

    @Mock
    private InventarioRepository inventarioRepository;

    @InjectMocks
    private InventarioServiceImpl inventarioService;

    // ── Helper: movimiento válido de base para los tests ───────────────────

    private Inventario movimientoValido() {
        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Arroz");
        producto.setStock(50);
        producto.setPrecio(1500.0);

        Inventario inventario = new Inventario();
        inventario.setProducto(producto);
        inventario.setTipoMovimiento("Entrada");
        inventario.setCantidad(20);
        inventario.setFechaMovimiento(new Date());
        return inventario;
    }

    // ── Tests CRUD existentes ──────────────────────────────────────────────

    @Test
    void testFindAll() {
        when(inventarioRepository.findAll()).thenReturn(List.of(new Inventario()));
        assertFalse(inventarioService.findAll().isEmpty());
    }

    @Test
    void testFindById() {
        Inventario i = new Inventario();
        i.setId(1L);
        when(inventarioRepository.findById(1L)).thenReturn(Optional.of(i));
        assertNotNull(inventarioService.findById(1L));
    }

    @Test
    void testSave() {
        Inventario i = movimientoValido();
        when(inventarioRepository.save(i)).thenReturn(i);
        assertNotNull(inventarioService.save(i));
    }

    @Test
    void testDeleteById() {
        doNothing().when(inventarioRepository).deleteById(1L);
        inventarioService.deleteById(1L);
        verify(inventarioRepository, times(1)).deleteById(1L);
    }

    @Test
    void testFindByProductoId() {
        when(inventarioRepository.findByProductoId(1L)).thenReturn(List.of(new Inventario()));
        assertFalse(inventarioService.findByProductoId(1L).isEmpty());
    }

    // ── Paso 3a — Información de Movimiento no nula ────────────────────────

    @Test
    void testMovimiento_tipoMovimientoYCantidadNoSonNulos() {
        Inventario inventario = movimientoValido();
        when(inventarioRepository.save(inventario)).thenReturn(inventario);

        Inventario resultado = inventarioService.save(inventario);

        assertAll("Campos de movimiento obligatorios",
            () -> assertNotNull(resultado.getTipoMovimiento()),
            () -> assertFalse(resultado.getTipoMovimiento().isBlank()),
            () -> assertNotNull(resultado.getCantidad()),
            () -> assertTrue(resultado.getCantidad() > 0)
        );
    }

    @Test
    void testMovimiento_tipoMovimientoEsValido() {
        Inventario entrada = movimientoValido();
        entrada.setTipoMovimiento("Entrada");

        Inventario salida = movimientoValido();
        salida.setTipoMovimiento("Salida");

        List<String> tiposValidos = List.of("Entrada", "Salida");
        assertTrue(tiposValidos.contains(entrada.getTipoMovimiento()));
        assertTrue(tiposValidos.contains(salida.getTipoMovimiento()));
    }

    // ── Paso 3b — Relación Producto-Inventario ──────────────────────────────

    @Test
    void testInventarioTieneProductoCorrecto() {
        Producto producto = new Producto();
        producto.setId(10L);
        producto.setNombre("Azúcar");
        producto.setStock(100);
        producto.setPrecio(800.0);

        Inventario inventario = new Inventario();
        inventario.setId(1L);
        inventario.setProducto(producto);
        inventario.setTipoMovimiento("Entrada");
        inventario.setCantidad(30);
        inventario.setFechaMovimiento(new Date());

        when(inventarioRepository.findById(1L)).thenReturn(Optional.of(inventario));

        Inventario resultado = inventarioService.findById(1L);

        assertNotNull(resultado.getProducto());
        assertEquals(10L, resultado.getProducto().getId());
        assertEquals("Azúcar", resultado.getProducto().getNombre());
    }

    // ── NUEVO: rutas negativas (retroalimentación del docente) ─────────────
    // El profesor pidió cubrir validaciones de tipo de movimiento inválido y
    // cantidad nula/negativa. La lógica de validación se implementó en
    // InventarioServiceImpl.save(); estos tests confirman que cada caso
    // se rechaza ANTES de llegar al repositorio.

    @Test
    void testGuardarMovimientoSinProductoLanzaExcepcion() {
        Inventario inventario = movimientoValido();
        inventario.setProducto(null);

        Exception ex = assertThrows(RuntimeException.class, () -> inventarioService.save(inventario));

        assertEquals("El movimiento de inventario debe estar asociado a un producto válido", ex.getMessage());
        verify(inventarioRepository, never()).save(any());
    }

    @Test
    void testGuardarMovimientoConProductoSinIdLanzaExcepcion() {
        Inventario inventario = movimientoValido();
        inventario.getProducto().setId(null);

        assertThrows(RuntimeException.class, () -> inventarioService.save(inventario));
        verify(inventarioRepository, never()).save(any());
    }

    @ParameterizedTest(name = "tipoMovimiento inválido = \"{0}\"")
    @ValueSource(strings = {"entrada", "SALIDA", "Traspaso", "Devolucion", "", " "})
    void testGuardarMovimientoConTipoMovimientoInvalidoLanzaExcepcion(String tipoInvalido) {
        Inventario inventario = movimientoValido();
        inventario.setTipoMovimiento(tipoInvalido);

        Exception ex = assertThrows(RuntimeException.class, () -> inventarioService.save(inventario));

        assertTrue(ex.getMessage().contains("Tipo de movimiento inválido"));
        verify(inventarioRepository, never()).save(any());
    }

    @Test
    void testGuardarMovimientoConTipoMovimientoNuloLanzaExcepcion() {
        Inventario inventario = movimientoValido();
        inventario.setTipoMovimiento(null);

        Exception ex = assertThrows(RuntimeException.class, () -> inventarioService.save(inventario));

        assertTrue(ex.getMessage().contains("Tipo de movimiento inválido"));
        verify(inventarioRepository, never()).save(any());
    }

    @ParameterizedTest(name = "cantidad inválida = {0}")
    @ValueSource(ints = {0, -1, -50})
    void testGuardarMovimientoConCantidadInvalidaLanzaExcepcion(int cantidadInvalida) {
        Inventario inventario = movimientoValido();
        inventario.setCantidad(cantidadInvalida);

        Exception ex = assertThrows(RuntimeException.class, () -> inventarioService.save(inventario));

        assertEquals("La cantidad del movimiento debe ser un valor positivo mayor a cero", ex.getMessage());
        verify(inventarioRepository, never()).save(any());
    }

    @Test
    void testGuardarMovimientoConCantidadNulaLanzaExcepcion() {
        Inventario inventario = movimientoValido();
        inventario.setCantidad(null);

        Exception ex = assertThrows(RuntimeException.class, () -> inventarioService.save(inventario));

        assertEquals("La cantidad del movimiento debe ser un valor positivo mayor a cero", ex.getMessage());
        verify(inventarioRepository, never()).save(any());
    }

    // ── NUEVO: pruebas paramétricas de límites de cantidad válidos ─────────
    // Cubre el borde exacto (cantidad = 1) y valores típicos de Entrada/Salida
    // para asegurar que los casos válidos en el límite no se rechacen por error.

    @ParameterizedTest(name = "tipoMovimiento=\"{0}\", cantidad={1} debe guardarse correctamente")
    @CsvSource({
        "Entrada, 1",
        "Entrada, 100",
        "Salida, 1",
        "Salida, 999"
    })
    void testGuardarMovimientoConCantidadYTipoEnElLimiteEsExitoso(String tipo, int cantidad) {
        Inventario inventario = movimientoValido();
        inventario.setTipoMovimiento(tipo);
        inventario.setCantidad(cantidad);

        when(inventarioRepository.save(inventario)).thenReturn(inventario);

        Inventario resultado = inventarioService.save(inventario);

        assertNotNull(resultado);
        assertEquals(tipo, resultado.getTipoMovimiento());
        assertEquals(cantidad, resultado.getCantidad());
        verify(inventarioRepository, times(1)).save(inventario);
    }

    // ── NUEVO: aserción reforzada con ArgumentCaptor (retroalimentación docente) ──
    // Verifica el objeto efectivamente enviado al repositorio, no solo que el
    // método haya sido invocado, confirmando que ninguna validación alteró
    // los datos originales del movimiento antes de persistir.

    @Test
    void testMovimientoPersistidoConservaDatosOriginales() {
        Inventario inventario = movimientoValido();
        inventario.setCantidad(45);
        inventario.setTipoMovimiento("Salida");
        when(inventarioRepository.save(any(Inventario.class))).thenReturn(inventario);

        ArgumentCaptor<Inventario> captor = ArgumentCaptor.forClass(Inventario.class);

        inventarioService.save(inventario);

        verify(inventarioRepository).save(captor.capture());
        Inventario persistido = captor.getValue();

        assertEquals("Salida", persistido.getTipoMovimiento());
        assertEquals(45, persistido.getCantidad());
        assertEquals(1L, persistido.getProducto().getId());
    }
}
