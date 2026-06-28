package com.minimarket;

import com.minimarket.entity.Producto;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.service.impl.ProductoServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoServiceImpl productoService;

    // ── Limpieza del contexto de seguridad entre pruebas ──────────────────

    @AfterEach
    void limpiarContextoSeguridad() {
        SecurityContextHolder.clearContext();
    }

    // ── Helpers para simular usuarios autenticados ────────────────────────

    private void autenticarComo(String rol) {
        var auth = new UsernamePasswordAuthenticationToken(
            "usuario_test",
            null,
            List.of(new SimpleGrantedAuthority(rol))
        );
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    // ── Tests CRUD básicos ────────────────────────────────────────────────

    @Test
    void testFindAll() {
        when(productoRepository.findAll()).thenReturn(List.of(new Producto()));
        assertFalse(productoService.findAll().isEmpty());
    }

    @Test
    void testFindById() {
        Producto p = new Producto();
        p.setId(1L);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(p));
        assertNotNull(productoService.findById(1L));
    }

    @Test
    void testFindByCategoriaId() {
        when(productoRepository.findByCategoriaId(1L)).thenReturn(List.of(new Producto()));
        assertFalse(productoService.findByCategoriaId(1L).isEmpty());
    }

    // ── Tests de autorización: modificar producto ─────────────────────────

    @Test
    void gerenteGuardarProducto_exitoso() {
        // Arrange
        autenticarComo("ROLE_GERENTE");

        Producto p = new Producto();
        p.setNombre("Arroz");
        p.setPrecio(1200.0);
        p.setStock(50);

        when(productoRepository.save(p)).thenReturn(p);

        // Act
        Producto resultado = productoService.save(p);

        // Assert
        assertNotNull(resultado);
        verify(productoRepository, times(1)).save(p);
    }

    @Test
    void empleadoGuardarProducto_exitoso() {
        // EMPLEADO también es administrador en este sistema
        autenticarComo("ROLE_EMPLEADO");

        Producto p = new Producto();
        p.setNombre("Leche");
        p.setPrecio(900.0);
        p.setStock(30);

        when(productoRepository.save(p)).thenReturn(p);

        Producto resultado = productoService.save(p);

        assertNotNull(resultado);
        verify(productoRepository, times(1)).save(p);
    }

    @Test
    void clienteGuardarProducto_lanzaSecurityException() {
        // Arrange
        autenticarComo("ROLE_CLIENTE");

        Producto p = new Producto();
        p.setNombre("Pan");

        // Act & Assert
        SecurityException ex = assertThrows(SecurityException.class,
            () -> productoService.save(p));

        assertEquals(
            "Acceso denegado: solo administradores pueden modificar productos",
            ex.getMessage()
        );

        // Nunca debe llegar al repositorio
        verify(productoRepository, never()).save(any());
    }

    @Test
    void sinAutenticarGuardarProducto_lanzaSecurityException() {
        // Sin llamar a autenticarComo() → SecurityContext vacío
        Producto p = new Producto();
        p.setNombre("Azucar");

        assertThrows(SecurityException.class,
            () -> productoService.save(p));

        verify(productoRepository, never()).save(any());
    }

    @Test
    void gerenteEliminarProducto_exitoso() {
        autenticarComo("ROLE_GERENTE");

        doNothing().when(productoRepository).deleteById(1L);

        productoService.deleteById(1L);

        verify(productoRepository, times(1)).deleteById(1L);
    }

    @Test
    void clienteEliminarProducto_lanzaSecurityException() {
        autenticarComo("ROLE_CLIENTE");

        SecurityException ex = assertThrows(SecurityException.class,
            () -> productoService.deleteById(1L));

        assertEquals(
            "Acceso denegado: solo administradores pueden modificar productos",
            ex.getMessage()
        );

        verify(productoRepository, never()).deleteById(any());
    }

    @Test
    void sinAutenticarEliminarProducto_lanzaSecurityException() {
        assertThrows(SecurityException.class,
            () -> productoService.deleteById(1L));

        verify(productoRepository, never()).deleteById(any());
    }
}