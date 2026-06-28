package com.minimarket;

import com.minimarket.entity.Categoria;
import com.minimarket.entity.DetalleVenta;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Usuario;
import com.minimarket.entity.Venta;
import com.minimarket.repository.CategoriaRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.service.VentaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prueba de integración (retroalimentación del docente: "verificar
 * transaccionalidad y rollback ante fallos parciales").
 *
 * Usa el contexto real de Spring con base de datos H2 en memoria, en lugar
 * de mocks, para comprobar que el método @Transactional VentaServiceImpl.save()
 * realmente revierte TODO el descuento de stock cuando uno de los productos
 * de la venta falla por stock insuficiente: el primer producto del detalle
 * alcanza a descontarse en memoria, pero como la transacción completa no
 * confirma (rollback), el stock persistido en la base de datos debe
 * permanecer igual al valor original.
 *
 * @Transactional a nivel de clase de test además revierte los datos de
 * prueba al finalizar cada @Test, evitando dejar residuos en la BD en memoria.
 */
@SpringBootTest
@Transactional
class VentaServiceTransaccionalidadIT {

    @Autowired
    private VentaService ventaService;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Producto productoConStock;
    private Producto productoSinStock;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        Categoria categoria = new Categoria();
        categoria.setNombre("Abarrotes-IT-" + System.nanoTime());
        categoria = categoriaRepository.save(categoria);

        productoConStock = new Producto();
        productoConStock.setNombre("Fideos");
        productoConStock.setPrecio(990.0);
        productoConStock.setStock(10);
        productoConStock.setCategoria(categoria);
        productoConStock = productoRepository.save(productoConStock);

        productoSinStock = new Producto();
        productoSinStock.setNombre("Atún");
        productoSinStock.setPrecio(1490.0);
        productoSinStock.setStock(1); // stock insuficiente a propósito
        productoSinStock.setCategoria(categoria);
        productoSinStock = productoRepository.save(productoSinStock);

        usuario = new Usuario();
        usuario.setUsername("cajero.it." + System.nanoTime());
        usuario.setPassword("hash");
        usuario.setNombre("Cajero");
        usuario.setApellido("IT");
        usuario.setEmail("cajero.it." + System.nanoTime() + "@test.com");
        usuario.setDireccion("Sucursal Centro");
        usuario.setRoles(new HashSet<>());
        usuario = usuarioRepository.save(usuario);
    }

    @Test
    void testVentaConProductoSinStock_revierteDescuentoDeStockDelProductoValido() {
        // Arrange: detalle1 tiene stock suficiente y se descuenta primero;
        // detalle2 (Atún) no tiene stock suficiente y debe fallar.
        DetalleVenta detalle1 = new DetalleVenta();
        detalle1.setProducto(productoConStock);
        detalle1.setCantidad(3); // hay 10, se pide 3 → OK

        DetalleVenta detalle2 = new DetalleVenta();
        detalle2.setProducto(productoSinStock);
        detalle2.setCantidad(5); // hay 1, se pide 5 → falla

        Venta venta = new Venta();
        venta.setUsuario(usuario);
        venta.setFecha(new Date());
        venta.setDetalles(Arrays.asList(detalle1, detalle2));
        detalle1.setVenta(venta);
        detalle2.setVenta(venta);

        // Act & Assert: la venta completa debe fallar por el segundo producto
        assertThrows(RuntimeException.class, () -> ventaService.save(venta));

        // Assert: gracias a @Transactional, el descuento de stock del PRIMER
        // producto (que sí tenía stock) NO debe haber quedado persistido,
        // porque la transacción completa se revirtió.
        Producto productoReleido = productoRepository.findById(productoConStock.getId()).orElseThrow();
        assertEquals(10, productoReleido.getStock(),
            "El stock del producto válido debe permanecer intacto tras el rollback de la transacción");
    }

    @Test
    void testVentaConAmbosProductosConStock_confirmaDescuentoDeAmbos() {
        DetalleVenta detalle1 = new DetalleVenta();
        detalle1.setProducto(productoConStock);
        detalle1.setCantidad(4);

        productoSinStock.setStock(20); // ahora sí hay stock suficiente
        productoRepository.save(productoSinStock);

        DetalleVenta detalle2 = new DetalleVenta();
        detalle2.setProducto(productoSinStock);
        detalle2.setCantidad(2);

        Venta venta = new Venta();
        venta.setUsuario(usuario);
        venta.setFecha(new Date());
        venta.setDetalles(Arrays.asList(detalle1, detalle2));
        detalle1.setVenta(venta);
        detalle2.setVenta(venta);

        Venta guardada = ventaService.save(venta);

        assertNotNull(guardada.getId());
        assertEquals(6, productoRepository.findById(productoConStock.getId()).orElseThrow().getStock());
        assertEquals(18, productoRepository.findById(productoSinStock.getId()).orElseThrow().getStock());
    }
}
