package com.minimarket.controller;

import com.minimarket.entity.*;
import com.minimarket.service.OrdenCompraService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrdenCompraControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrdenCompraService ordenCompraService;

    private OrdenCompra construirOrden() {
        Proveedor proveedor = new Proveedor();
        proveedor.setId(1L);
        proveedor.setNombre("Distribuidora Andina SpA");
        proveedor.setRut("76.123.456-7");

        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Leche Entera 1L");
        producto.setPrecio(1200.0);

        Sucursal sucursal = new Sucursal();
        sucursal.setId(1L);
        sucursal.setNombre("Sucursal Centro");
        sucursal.setDireccion("Av. Principal 123");

        OrdenCompra orden = new OrdenCompra();
        orden.setId(1L);
        orden.setProveedor(proveedor);
        orden.setProducto(producto);
        orden.setSucursal(sucursal);
        orden.setCantidadSolicitada(30);
        orden.setEstado(EstadoOrdenCompra.PENDIENTE);
        orden.setAutomatica(true);
        return orden;
    }

    @Test
    @WithMockUser(roles = {"GERENTE"})
    void gerente_puedeListarOrdenes() throws Exception {
        when(ordenCompraService.listarTodas()).thenReturn(List.of(construirOrden()));

        mockMvc.perform(get("/api/ordenes-compra"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"JEFE_TURNO"})
    void jefeTurno_puedeListarOrdenes() throws Exception {
        when(ordenCompraService.listarTodas()).thenReturn(List.of(construirOrden()));

        mockMvc.perform(get("/api/ordenes-compra"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"REPONEDOR"})
    void reponedor_NoPuedeListarOrdenes_devuelve403() throws Exception {
        mockMvc.perform(get("/api/ordenes-compra"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"CLIENTE"})
    void cliente_NoPuedeListarOrdenes_devuelve403() throws Exception {
        mockMvc.perform(get("/api/ordenes-compra"))
                .andExpect(status().isForbidden());
    }

    @Test
    void sinAutenticar_NoPuedeListarOrdenes_devuelve401() throws Exception {
        mockMvc.perform(get("/api/ordenes-compra"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = {"GERENTE"})
    void gerente_puedeCambiarEstado() throws Exception {
        OrdenCompra recibida = construirOrden();
        recibida.setEstado(EstadoOrdenCompra.RECIBIDA);

        when(ordenCompraService.cambiarEstado(1L, EstadoOrdenCompra.RECIBIDA)).thenReturn(recibida);

        mockMvc.perform(patch("/api/ordenes-compra/1/estado").param("estado", "RECIBIDA"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"REPONEDOR"})
    void reponedor_NoPuedeCambiarEstado_devuelve403() throws Exception {
        mockMvc.perform(patch("/api/ordenes-compra/1/estado").param("estado", "RECIBIDA"))
                .andExpect(status().isForbidden());
    }
}
