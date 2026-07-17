package com.minimarket.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimarket.assembler.CarritoModelAssembler;
import com.minimarket.entity.Carrito;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Usuario;
import com.minimarket.service.CarritoService;
import com.minimarket.service.ProductoService;
import com.minimarket.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CarritoControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock private CarritoService carritoService;
    @Mock private CarritoModelAssembler carritoModelAssembler;
    @Mock private ProductoService productoService;
    @Mock private UsuarioService usuarioService;

    private CarritoController carritoController;

    private Carrito carrito;
    private Usuario usuario;
    private Producto producto;

    @BeforeEach
    void setUp() {
        // Se instancia manualmente (en vez de @InjectMocks) para garantizar que
        // los 4 mocks queden cableados de forma determinista: con @InjectMocks
        // se observó que usuarioService podía quedar null y provocar un NPE
        // en agregarProductoAlCarrito/actualizarCarrito.
        carritoController = new CarritoController(
                carritoService, carritoModelAssembler, productoService, usuarioService);
        mockMvc = MockMvcBuilders.standaloneSetup(carritoController).build();
        objectMapper = new ObjectMapper();

        usuario = new Usuario();
        usuario.setId(1L);

        producto = new Producto();
        producto.setId(1L);

        carrito = new Carrito();
        carrito.setId(1L);
        carrito.setUsuario(usuario);
        carrito.setProducto(producto);
        carrito.setCantidad(2);

        // El assembler real se prueba de forma unitaria aparte; aquí solo se
        // simula la conversión a EntityModel para evitar NullPointerException.
        lenient().when(carritoModelAssembler.toModel(any(Carrito.class)))
                .thenAnswer(invocation -> EntityModel.of(invocation.getArgument(0)));

        // Usados por agregarProductoAlCarrito/actualizarCarrito para resolver
        // el usuario y producto reales a partir de los IDs del payload.
        lenient().when(usuarioService.findById(1L)).thenReturn(java.util.Optional.of(usuario));
        lenient().when(productoService.findById(1L)).thenReturn(producto);
    }

    @Test
    void testListarCarrito() throws Exception {
        when(carritoService.findAll()).thenReturn(List.of(carrito));
        mockMvc.perform(get("/api/carrito"))
                .andExpect(status().isOk());
    }

    @Test
    void testObtenerCarritoPorId_Existente() throws Exception {
        when(carritoService.findById(1L)).thenReturn(carrito);
        mockMvc.perform(get("/api/carrito/1"))
                .andExpect(status().isOk());
    }

    @Test
    void testObtenerCarritoPorId_Inexistente() throws Exception {
        when(carritoService.findById(99L)).thenReturn(null);
        mockMvc.perform(get("/api/carrito/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testAgregarProductoAlCarrito() throws Exception {
        when(carritoService.save(any(Carrito.class))).thenReturn(carrito);
        mockMvc.perform(post("/api/carrito")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(carrito)))
                .andExpect(status().isCreated()); 
    }

    @Test
    void testActualizarCarrito_Existente() throws Exception {
        when(carritoService.findById(1L)).thenReturn(carrito);
        when(carritoService.save(any(Carrito.class))).thenReturn(carrito);

        mockMvc.perform(put("/api/carrito/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(carrito)))
                .andExpect(status().isOk());
    }

    @Test
    void testActualizarCarrito_Inexistente() throws Exception {
        when(carritoService.findById(1L)).thenReturn(null);

        mockMvc.perform(put("/api/carrito/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(carrito)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testEliminarProductoDelCarrito_Existente() throws Exception {
        when(carritoService.findById(1L)).thenReturn(carrito);
        doNothing().when(carritoService).deleteById(1L);

        mockMvc.perform(delete("/api/carrito/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testEliminarProductoDelCarrito_Inexistente() throws Exception {
        when(carritoService.findById(1L)).thenReturn(null);

        mockMvc.perform(delete("/api/carrito/1"))
                .andExpect(status().isNotFound());
    }
}