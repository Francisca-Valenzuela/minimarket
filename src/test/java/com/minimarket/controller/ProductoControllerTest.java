package com.minimarket.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimarket.assembler.ProductoModelAssembler;
import com.minimarket.dto.ProductoDTO;
import com.minimarket.entity.Categoria;
import com.minimarket.entity.Producto;
import com.minimarket.service.CategoriaService;
import com.minimarket.service.ProductoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(MockitoExtension.class)
class ProductoControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock private ProductoService productoService;
    @Mock private CategoriaService categoriaService;
    @Mock private ProductoModelAssembler productoModelAssembler;

    @InjectMocks
    private ProductoController productoController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productoController).build();
        objectMapper = new ObjectMapper();
        // Debe tener "lenient()" — sin esto, Mockito exige que el stub se
        // use en TODOS los tests del archivo, y falla si alguno no lo usa
        lenient().when(productoModelAssembler.toModel(any(Producto.class)))
                .thenAnswer(invocation -> EntityModel.of(invocation.getArgument(0)));
    }

    @Test
    void testListarProductos() throws Exception {
        when(productoService.findAll()).thenReturn(List.of(new Producto()));

        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk());
    }

    @Test
    void testObtenerProductoPorId_Existente() throws Exception {
        Producto producto = new Producto();
        producto.setId(1L);
        when(productoService.findById(1L)).thenReturn(producto);

        mockMvc.perform(get("/api/productos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void testObtenerProductoPorId_Inexistente() throws Exception {
        when(productoService.findById(99L)).thenReturn(null);

        mockMvc.perform(get("/api/productos/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGuardarProducto_CategoriaNoExiste() throws Exception {
        ProductoDTO dto = new ProductoDTO();
        dto.setCategoriaId(99L);
        dto.setNombre("Arroz");
        dto.setPrecio(1000.0);
        dto.setStock(10);

        when(categoriaService.findById(99L)).thenReturn(null);

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGuardarProducto_Exitoso() throws Exception {
        ProductoDTO dto = new ProductoDTO();
        dto.setCategoriaId(1L);
        dto.setNombre("Arroz");
        dto.setPrecio(1000.0);
        dto.setStock(10);

        Producto guardado = new Producto();
        guardado.setId(1L);

        when(categoriaService.findById(1L)).thenReturn(new Categoria());
        when(productoService.save(any(Producto.class))).thenReturn(guardado);

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    void testActualizarProducto_Existente() throws Exception {
        Producto productoExistente = new Producto();
        productoExistente.setId(1L);

        ProductoDTO dto = new ProductoDTO();
        dto.setCategoriaId(1L);
        dto.setNombre("Arroz Modificado");
        dto.setPrecio(1200.0);
        dto.setStock(15);

        when(productoService.findById(1L)).thenReturn(productoExistente);
        when(categoriaService.findById(1L)).thenReturn(new Categoria());
        when(productoService.save(any(Producto.class))).thenReturn(productoExistente);

        mockMvc.perform(put("/api/productos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void testActualizarProducto_Inexistente() throws Exception {
        ProductoDTO dto = new ProductoDTO();
        dto.setCategoriaId(1L);
        dto.setNombre("Arroz Modificado");
        dto.setPrecio(1200.0);
        dto.setStock(15);

        when(productoService.findById(1L)).thenReturn(null);

        mockMvc.perform(put("/api/productos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testEliminarProducto_Existente() throws Exception {
        when(productoService.findById(1L)).thenReturn(new Producto());
        doNothing().when(productoService).deleteById(1L);

        mockMvc.perform(delete("/api/productos/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testEliminarProducto_Inexistente() throws Exception {
        when(productoService.findById(1L)).thenReturn(null);

        mockMvc.perform(delete("/api/productos/1"))
                .andExpect(status().isNotFound());
    }
}