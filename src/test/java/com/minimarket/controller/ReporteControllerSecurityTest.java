package com.minimarket.controller;

import com.minimarket.dto.RotacionProductoDTO;
import com.minimarket.service.ReporteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ReporteControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReporteService reporteService;

    @Test
    @WithMockUser(roles = {"GERENTE"})
    void gerente_puedeVerReporteDeRotacion() throws Exception {
        when(reporteService.getRotacionProductos(any(), any()))
                .thenReturn(List.of(new RotacionProductoDTO(1L, "Leche Entera 1L", 50L, 60000.0)));

        mockMvc.perform(get("/api/reportes/rotacion")
                        .param("fechaInicio", "2026-01-01")
                        .param("fechaFin", "2026-07-16"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"EMPLEADO"})
    void empleado_NoPuedeVerReporteDeRotacion_devuelve403() throws Exception {
        mockMvc.perform(get("/api/reportes/rotacion")
                        .param("fechaInicio", "2026-01-01")
                        .param("fechaFin", "2026-07-16"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"CLIENTE"})
    void cliente_NoPuedeVerReporteDeRotacion_devuelve403() throws Exception {
        mockMvc.perform(get("/api/reportes/rotacion")
                        .param("fechaInicio", "2026-01-01")
                        .param("fechaFin", "2026-07-16"))
                .andExpect(status().isForbidden());
    }

    @Test
    void sinAutenticar_NoPuedeVerReporteDeRotacion_devuelve401() throws Exception {
        mockMvc.perform(get("/api/reportes/rotacion")
                        .param("fechaInicio", "2026-01-01")
                        .param("fechaFin", "2026-07-16"))
                .andExpect(status().isUnauthorized());
    }
}
