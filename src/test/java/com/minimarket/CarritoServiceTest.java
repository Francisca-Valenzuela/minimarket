package com.minimarket;

import com.minimarket.entity.Carrito;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.CarritoRepository;
import com.minimarket.service.impl.CarritoServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarritoServiceTest {

    @Mock
    private CarritoRepository carritoRepository;

    @InjectMocks
    private CarritoServiceImpl carritoService;

    // ── Tests CRUD existentes ──────────────────────────────────────────────

    @Test
    void testFindAll() {
        when(carritoRepository.findAll()).thenReturn(List.of(new Carrito()));
        assertFalse(carritoService.findAll().isEmpty());
    }

    @Test
    void testFindById() {
        Carrito c = new Carrito();
        c.setId(1L);
        when(carritoRepository.findById(1L)).thenReturn(Optional.of(c));
        assertNotNull(carritoService.findById(1L));
    }

    @Test
    void testSave() {
        Carrito c = new Carrito();
        when(carritoRepository.save(c)).thenReturn(c);
        assertNotNull(carritoService.save(c));
    }

    @Test
    void testDeleteById() {
        doNothing().when(carritoRepository).deleteById(1L);
        carritoService.deleteById(1L);
        verify(carritoRepository, times(1)).deleteById(1L);
    }

    @Test
    void testFindByUsuarioId() {
        when(carritoRepository.findByUsuarioId(1L)).thenReturn(List.of(new Carrito()));
        assertFalse(carritoService.findByUsuarioId(1L).isEmpty());
    }

    // ── S5: Paso 2a — Disponibilidad de stock ─────────────────────────

    @Test
    void testAgregarProducto_conStockSuficiente_guardaCarrito() {
        // Arrange
        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Arroz");
        producto.setStock(10);   // hay 10 en stock
        producto.setPrecio(1500.0);

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Francisca");

        Carrito carrito = new Carrito();
        carrito.setProducto(producto);
        carrito.setUsuario(usuario);
        carrito.setCantidad(3);  // pide 3, hay 10 → debe pasar

        when(carritoRepository.save(carrito)).thenReturn(carrito);

        // Act
        Carrito resultado = carritoService.agregarProducto(carrito);

        // Assert
        assertNotNull(resultado);
        verify(carritoRepository, times(1)).save(carrito);
    }

    @Test
    void testAgregarProducto_conStockInsuficiente_lanzaExcepcion() {
        // Arrange
        Producto producto = new Producto();
        producto.setId(2L);
        producto.setNombre("Leche");
        producto.setStock(1);    // solo hay 1 en stock
        producto.setPrecio(900.0);

        Carrito carrito = new Carrito();
        carrito.setProducto(producto);
        carrito.setCantidad(5);  // pide 5, hay 1 → debe fallar

        // Act & Assert
        assertThrows(RuntimeException.class,
            () -> carritoService.agregarProducto(carrito));

        // Nunca debe llegar a guardar
        verify(carritoRepository, never()).save(any());
    }

    // ── s5: Paso 2b — Relación Producto-Usuario ────────────────────────

    @Test
    void testCarritoTieneUsuarioCorrecto() {
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setId(42L);
        usuario.setNombre("Francisca");
        usuario.setUsername("fvalenzuela");

        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Pan");
        producto.setStock(20);
        producto.setPrecio(500.0);

        Carrito carrito = new Carrito();
        carrito.setId(1L);
        carrito.setUsuario(usuario);
        carrito.setProducto(producto);
        carrito.setCantidad(2);

        when(carritoRepository.findById(1L)).thenReturn(Optional.of(carrito));

        // Act
        Carrito resultado = carritoService.findById(1L);

        // Assert
        assertNotNull(resultado.getUsuario());
        assertEquals(42L, resultado.getUsuario().getId());
        assertEquals("fvalenzuela", resultado.getUsuario().getUsername());
    }
}