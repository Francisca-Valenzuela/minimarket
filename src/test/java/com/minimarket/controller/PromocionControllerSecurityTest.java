package com.minimarket.controller;

import com.minimarket.entity.Producto;
import com.minimarket.entity.Promocion;
import com.minimarket.entity.TipoDescuento;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.PromocionRepository;
import com.minimarket.repository.SucursalRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PromocionControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PromocionRepository promocionRepository;

    @MockitoBean
    private ProductoRepository productoRepository;

    @MockitoBean
    private SucursalRepository sucursalRepository;

    private Promocion construirPromocion() {
        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Leche Entera 1L");
        producto.setPrecio(1200.0);

        Promocion promo = new Promocion();
        promo.setId(1L);
        promo.setProducto(producto);
        promo.setTipoDescuento(TipoDescuento.PORCENTAJE);
        promo.setValor(15.0);
        promo.setFechaInicio(LocalDate.now());
        promo.setFechaFin(LocalDate.now().plusDays(10));
        promo.setActiva(true);
        return promo;
    }

    @Test
    @WithMockUser(roles = {"CLIENTE"})
    void cualquierAutenticado_puedeListarPromociones() throws Exception {
        when(promocionRepository.findAll()).thenReturn(List.of(construirPromocion()));

        mockMvc.perform(get("/api/promociones"))
                .andExpect(status().isOk());
    }

    @Test
    void sinAutenticar_NoPuedeListarPromociones_devuelve401() throws Exception {
        mockMvc.perform(get("/api/promociones"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = {"GERENTE"})
    void gerente_puedeCrearPromocion() throws Exception {
        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Leche Entera 1L");
        producto.setPrecio(1200.0);

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(promocionRepository.save(any(Promocion.class))).thenAnswer(inv -> {
            Promocion p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        mockMvc.perform(post("/api/promociones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productoId\":1,\"tipoDescuento\":\"PORCENTAJE\",\"valor\":15.0," +
                                "\"fechaInicio\":\"2026-07-16\",\"fechaFin\":\"2026-07-31\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"EMPLEADO"})
    void empleado_NoPuedeCrearPromocion_devuelve403() throws Exception {
        mockMvc.perform(post("/api/promociones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productoId\":1,\"tipoDescuento\":\"PORCENTAJE\",\"valor\":15.0," +
                                "\"fechaInicio\":\"2026-07-16\",\"fechaFin\":\"2026-07-31\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"GERENTE"})
    void gerente_puedeDesactivarPromocion() throws Exception {
        Promocion promo = construirPromocion();
        when(promocionRepository.findById(1L)).thenReturn(Optional.of(promo));
        when(promocionRepository.save(any(Promocion.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(patch("/api/promociones/1/desactivar"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"CLIENTE"})
    void cliente_NoPuedeDesactivarPromocion_devuelve403() throws Exception {
        mockMvc.perform(patch("/api/promociones/1/desactivar"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"GERENTE"})
    void gerente_puedeEliminarPromocion() throws Exception {
        mockMvc.perform(delete("/api/promociones/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = {"CLIENTE"})
    void cliente_NoPuedeEliminarPromocion_devuelve403() throws Exception {
        mockMvc.perform(delete("/api/promociones/1"))
                .andExpect(status().isForbidden());
    }
}
