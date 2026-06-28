package com.minimarket.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimarket.dto.ProductoDTO;
import com.minimarket.entity.Categoria;
import com.minimarket.entity.Producto;
import com.minimarket.service.CategoriaService;
import com.minimarket.service.ProductoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests de AUTORIZACIÓN por rol para ProductoController.
 * A diferencia de ProductoControllerTest (que usa standaloneSetup y NO
 * evalúa @PreAuthorize), aquí se levanta el contexto de seguridad real
 * con @SpringBootTest + @AutoConfigureMockMvc, de modo que @WithMockUser
 * pasa realmente por SecurityConfig y por las anotaciones @PreAuthorize
 * de los controladores.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ProductoControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductoService productoService;

    @MockitoBean
    private CategoriaService categoriaService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @WithMockUser(roles = {"EMPLEADO"})
    void empleado_puedeListarProductos() throws Exception {
        when(productoService.findAll()).thenReturn(java.util.List.of(new Producto()));

        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"GERENTE"})
    void gerente_puedeCrearProducto() throws Exception {
        ProductoDTO dto = new ProductoDTO();
        dto.setCategoriaId(1L);
        dto.setNombre("Arroz");
        dto.setPrecio(1000.0);
        dto.setStock(10);

        when(categoriaService.findById(1L)).thenReturn(new Categoria());
        when(productoService.save(any(Producto.class))).thenReturn(new Producto());

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"CLIENTE"})
    void cliente_NoPuedeCrearProducto_devuelve403() throws Exception {
        ProductoDTO dto = new ProductoDTO();
        dto.setCategoriaId(1L);
        dto.setNombre("Arroz");
        dto.setPrecio(1000.0);
        dto.setStock(10);

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void sinAutenticar_NoPuedeListarProductos_devuelve401() throws Exception {
        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isUnauthorized());
    }
}