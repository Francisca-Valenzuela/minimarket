package com.minimarket.controller;

import com.minimarket.dto.DisponibilidadDTO;
import com.minimarket.entity.Producto;
import com.minimarket.entity.StockSucursal;
import com.minimarket.entity.Sucursal;
import com.minimarket.service.SucursalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests de AUTORIZACIÓN por rol para SucursalController, al mismo estilo
 * que ProductoControllerSecurityTest: @SpringBootTest + @AutoConfigureMockMvc
 * para que @WithMockUser pase realmente por SecurityConfig y @PreAuthorize.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SucursalControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SucursalService sucursalService;

    @Test
    @WithMockUser(roles = {"CLIENTE"})
    void cualquierAutenticado_puedeListarSucursales() throws Exception {
        when(sucursalService.listarActivas()).thenReturn(List.of(new Sucursal()));

        mockMvc.perform(get("/api/sucursales"))
                .andExpect(status().isOk());
    }

    @Test
    void sinAutenticar_NoPuedeListarSucursales_devuelve401() throws Exception {
        mockMvc.perform(get("/api/sucursales"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = {"GERENTE"})
    void gerente_puedeCrearSucursal() throws Exception {
        Sucursal creada = new Sucursal();
        creada.setId(1L);
        creada.setNombre("Sucursal Nueva");
        creada.setDireccion("Calle Falsa 123");
        creada.setComuna("Santiago");
        creada.setActiva(true);

        when(sucursalService.crear(any(Sucursal.class))).thenReturn(creada);

        mockMvc.perform(post("/api/sucursales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Sucursal Nueva\",\"direccion\":\"Calle Falsa 123\",\"comuna\":\"Santiago\",\"activa\":true}"))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = {"CLIENTE"})
    void cliente_NoPuedeCrearSucursal_devuelve403() throws Exception {
        mockMvc.perform(post("/api/sucursales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Sucursal Nueva\",\"direccion\":\"Calle Falsa 123\",\"comuna\":\"Santiago\",\"activa\":true}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"REPONEDOR"})
    void reponedor_puedeVerStockDeSucursal() throws Exception {
        when(sucursalService.listarStockPorSucursal(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/sucursales/1/stock"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"CLIENTE"})
    void cliente_NoPuedeVerStockDeSucursal_devuelve403() throws Exception {
        mockMvc.perform(get("/api/sucursales/1/stock"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"JEFE_TURNO"})
    void jefeTurno_puedeAsignarStock() throws Exception {
        Sucursal sucursal = new Sucursal();
        sucursal.setId(1L);
        sucursal.setNombre("Sucursal Centro");

        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Leche Entera 1L");

        StockSucursal stock = new StockSucursal();
        stock.setId(1L);
        stock.setSucursal(sucursal);
        stock.setProducto(producto);
        stock.setCantidad(40);
        stock.setStockMinimo(10);

        when(sucursalService.asignarStock(1L, 1L, 40, 10)).thenReturn(stock);

        mockMvc.perform(post("/api/sucursales/1/stock/1")
                        .param("cantidad", "40")
                        .param("stockMinimo", "10"))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = {"REPONEDOR"})
    void reponedor_NoPuedeAsignarStock_devuelve403() throws Exception {
        // asignarStock es GERENTE/JEFE_TURNO; REPONEDOR solo puede ajustar, no asignar
        mockMvc.perform(post("/api/sucursales/1/stock/1")
                        .param("cantidad", "40")
                        .param("stockMinimo", "10"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"CLIENTE"})
    void cualquierAutenticado_puedeConsultarDisponibilidad() throws Exception {
        when(sucursalService.consultarDisponibilidad(1L)).thenReturn(List.of(new DisponibilidadDTO()));

        mockMvc.perform(get("/api/sucursales/disponibilidad/1"))
                .andExpect(status().isOk());
    }
}
