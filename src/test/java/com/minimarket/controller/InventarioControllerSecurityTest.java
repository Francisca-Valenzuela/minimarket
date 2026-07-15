package com.minimarket.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimarket.entity.Inventario;
import com.minimarket.entity.Producto;
import com.minimarket.service.InventarioService;
import org.junit.jupiter.api.Test;
import java.util.Date;
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
        Inventario inventario = new Inventario();
        inventario.setId(1L);
        inventario.setCantidad(50);
        inventario.setTipoMovimiento("Entrada");
        // CORRECCIÓN: la entidad exige @NotNull en fechaMovimiento (Bean Validation
        // se ejecuta antes que @PreAuthorize), por lo que sin este campo el request
        // nunca llegaba a evaluar el rol y fallaba con 400 en vez de 201.
        inventario.setFechaMovimiento(new Date());

        // Le agregamos un producto simulado para evitar el NullPointerException en HATEOAS
        Producto producto = new Producto();
        producto.setId(1L);
        inventario.setProducto(producto);

        when(inventarioService.save(any(Inventario.class))).thenReturn(inventario);

        mockMvc.perform(post("/api/inventario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inventario)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = {"EMPLEADO"})
    void empleado_NoPuedeRegistrarMovimiento_devuelve403() throws Exception {
        // CORRECCIÓN: se completa el payload con todos los campos requeridos
        // (producto, cantidad, tipoMovimiento, fechaMovimiento) para que el request
        // sea válido y el 403 provenga realmente de la verificación de rol y no
        // de un 400 por validación de datos incompletos.
        Inventario inventario = new Inventario();
        inventario.setId(1L);
        inventario.setCantidad(10);
        inventario.setTipoMovimiento("Entrada");
        inventario.setFechaMovimiento(new Date());

        Producto producto = new Producto();
        producto.setId(1L);
        inventario.setProducto(producto);

        mockMvc.perform(post("/api/inventario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inventario)))
                .andExpect(status().isForbidden());
    }
}