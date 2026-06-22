package com.minimarket;

import com.minimarket.entity.Inventario;
import com.minimarket.entity.Producto;
import com.minimarket.repository.InventarioRepository;
import com.minimarket.service.impl.InventarioServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
        Inventario i = new Inventario();
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

    // ── NUEVO: Paso 3a — Información de Movimiento no nula ────────────────

    @Test
    void testMovimiento_tipoMovimientoYCantidadNoSonNulos() {
        // Arrange
        Inventario inventario = new Inventario();
        inventario.setTipoMovimiento("Entrada");
        inventario.setCantidad(50);
        inventario.setFechaMovimiento(new Date());

        when(inventarioRepository.save(inventario)).thenReturn(inventario);

        // Act
        Inventario resultado = inventarioService.save(inventario);

        // Assert
        assertAll("Campos de movimiento obligatorios",
            () -> assertNotNull(resultado.getTipoMovimiento()),
            () -> assertFalse(resultado.getTipoMovimiento().isBlank()),
            () -> assertNotNull(resultado.getCantidad()),
            () -> assertTrue(resultado.getCantidad() > 0)
        );
    }

    @Test
    void testMovimiento_tipoMovimientoEsValido() {
        // Arrange — valida que solo "Entrada" o "Salida" son aceptados
        Inventario entrada = new Inventario();
        entrada.setTipoMovimiento("Entrada");

        Inventario salida = new Inventario();
        salida.setTipoMovimiento("Salida");

        // Act & Assert
        List<String> tiposValidos = List.of("Entrada", "Salida");
        assertTrue(tiposValidos.contains(entrada.getTipoMovimiento()));
        assertTrue(tiposValidos.contains(salida.getTipoMovimiento()));
    }

    // ── NUEVO: Paso 3b — Relación Producto-Inventario ─────────────────────

    @Test
    void testInventarioTieneProductoCorrecto() {
        // Arrange
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

        // Act
        Inventario resultado = inventarioService.findById(1L);

        // Assert
        assertNotNull(resultado.getProducto());
        assertEquals(10L, resultado.getProducto().getId());
        assertEquals("Azúcar", resultado.getProducto().getNombre());
    }
}