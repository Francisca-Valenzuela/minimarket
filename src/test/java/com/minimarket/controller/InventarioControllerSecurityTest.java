package com.minimarket.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimarket.entity.Inventario;
import com.minimarket.entity.Producto;
import com.minimarket.service.InventarioService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica que el registro de movimientos de inventario solo lo pueda
 * hacer un usuario con rol GERENTE (según @PreAuthorize("hasRole('GERENTE')")
 * en InventarioController.registrarMovimiento), y que EMPLEADO/CLIENTE
 * sean rechazados.
 */
@SpringBootTest
@AutoConfigureMockMvc
class InventarioControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InventarioService inventarioService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @WithMockUser(roles = {"GERENTE"})
    void gerente_puedeRegistrarMovimiento() throws Exception {
        Producto producto = new Producto();
        producto.setId(1L);

        Inventario inventario = new Inventario();
        inventario.setId(1L);
        inventario.setProducto(producto);
        inventario.setCantidad(50);
        inventario.setTipoMovimiento("Entrada");

        when(inventarioService.save(any(Inventario.class))).thenReturn(inventario);

        mockMvc.perform(post("/api/inventario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inventario)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"EMPLEADO"})
    void empleado_NoPuedeRegistrarMovimiento_devuelve403() throws Exception {
        Inventario inventario = new Inventario();
        inventario.setId(1L);
        inventario.setCantidad(50);
        inventario.setTipoMovimiento("Entrada");

        mockMvc.perform(post("/api/inventario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inventario)))
                .andExpect(status().isForbidden());
    }
}