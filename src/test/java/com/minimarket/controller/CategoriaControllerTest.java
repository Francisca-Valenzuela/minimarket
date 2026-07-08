package com.minimarket.controller;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimarket.assembler.CategoriaModelAssembler;
import com.minimarket.entity.Categoria;
import com.minimarket.service.CategoriaService;

@ExtendWith(MockitoExtension.class)
class CategoriaControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private CategoriaService categoriaService;

    @InjectMocks
    private CategoriaController categoriaController;

    private Categoria categoria;

    @Mock private CategoriaModelAssembler assembler;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(categoriaController).build();
        objectMapper = new ObjectMapper();

        categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNombre("Bebidas");
        lenient().when(assembler.toModel(any(Categoria.class)))
         .thenAnswer(invocation -> EntityModel.of(invocation.getArgument(0)));
    }

    @Test
    void testListarCategorias() throws Exception {
        when(categoriaService.findAll()).thenReturn(List.of(categoria));

        mockMvc.perform(get("/api/categorias")) // <--- Corregido
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nombre").value("Bebidas"));
    }

    @Test
    void testObtenerCategoriaPorId_Existente() throws Exception {
        when(categoriaService.findById(1L)).thenReturn(categoria);

        mockMvc.perform(get("/api/categorias/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Bebidas"));
    }

    @Test
    void testObtenerCategoriaPorId_Inexistente() throws Exception {
        when(categoriaService.findById(99L)).thenReturn(null);

        mockMvc.perform(get("/api/categorias/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGuardarCategoria() throws Exception {
        when(categoriaService.save(any(Categoria.class))).thenReturn(categoria);

        mockMvc.perform(post("/api/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoria)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Bebidas"));
    }

    @Test
    void testActualizarCategoria_Existente() throws Exception {
        when(categoriaService.findById(1L)).thenReturn(categoria);
        when(categoriaService.save(any(Categoria.class))).thenReturn(categoria);

        mockMvc.perform(put("/api/categorias/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoria)))
                .andExpect(status().isOk());
    }

    @Test
    void testActualizarCategoria_Inexistente() throws Exception {
        when(categoriaService.findById(1L)).thenReturn(null);

        mockMvc.perform(put("/api/categorias/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoria)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testEliminarCategoria_Existente() throws Exception {
        when(categoriaService.findById(1L)).thenReturn(categoria);
        doNothing().when(categoriaService).deleteById(1L);

        mockMvc.perform(delete("/api/categorias/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testEliminarCategoria_Inexistente() throws Exception {
        when(categoriaService.findById(1L)).thenReturn(null);

        mockMvc.perform(delete("/api/categorias/1"))
                .andExpect(status().isNotFound());
    }
}