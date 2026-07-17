package com.minimarket.controller;

import com.minimarket.entity.Proveedor;
import com.minimarket.repository.ProveedorRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProveedorControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProveedorRepository proveedorRepository;

    @Test
    @WithMockUser(roles = {"JEFE_TURNO"})
    void jefeTurno_puedeListarProveedores() throws Exception {
        when(proveedorRepository.findByActivoTrue()).thenReturn(List.of(new Proveedor()));

        mockMvc.perform(get("/api/proveedores"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"CLIENTE"})
    void cliente_NoPuedeListarProveedores_devuelve403() throws Exception {
        mockMvc.perform(get("/api/proveedores"))
                .andExpect(status().isForbidden());
    }

    @Test
    void sinAutenticar_NoPuedeListarProveedores_devuelve401() throws Exception {
        mockMvc.perform(get("/api/proveedores"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = {"GERENTE"})
    void gerente_puedeCrearProveedor() throws Exception {
        Proveedor guardado = new Proveedor();
        guardado.setId(1L);
        guardado.setNombre("Distribuidora Andina SpA");
        guardado.setRut("76.123.456-7");
        guardado.setActivo(true);

        when(proveedorRepository.save(any(Proveedor.class))).thenReturn(guardado);

        mockMvc.perform(post("/api/proveedores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Distribuidora Andina SpA\",\"rut\":\"76.123.456-7\",\"activo\":true}"))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = {"JEFE_TURNO"})
    void jefeTurno_NoPuedeCrearProveedor_devuelve403() throws Exception {
        // Listar es GERENTE/JEFE_TURNO, pero crear es solo GERENTE
        mockMvc.perform(post("/api/proveedores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Distribuidora Andina SpA\",\"rut\":\"76.123.456-7\",\"activo\":true}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"GERENTE"})
    void gerente_puedeActualizarProveedor() throws Exception {
        Proveedor existente = new Proveedor();
        existente.setId(1L);
        existente.setNombre("Distribuidora Andina SpA");
        existente.setRut("76.123.456-7");
        existente.setActivo(true);

        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(proveedorRepository.save(any(Proveedor.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(put("/api/proveedores/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Distribuidora Andina SpA\",\"rut\":\"76.123.456-7\",\"activo\":false}"))
                .andExpect(status().isOk());
    }
}
