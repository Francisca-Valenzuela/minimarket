package com.minimarket.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimarket.dto.VentaResponseDTO; 
import com.minimarket.entity.DetalleVenta;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Usuario;
import com.minimarket.entity.Venta;
import com.minimarket.service.VentaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.minimarket.entity.Sucursal;
import com.minimarket.entity.TipoEntrega;


import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.hateoas.EntityModel;
import com.minimarket.assembler.VentaModelAssembler;

@ExtendWith(MockitoExtension.class)
class VentaControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private VentaService ventaService;

    @InjectMocks
    private VentaController ventaController;

    private Venta venta;

    @Mock
    private VentaModelAssembler assembler;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(ventaController).build();
        objectMapper = new ObjectMapper();

        Usuario usuario = new Usuario();
        usuario.setId(1L);

        Producto producto = new Producto();
        producto.setId(1L);

        DetalleVenta detalle = new DetalleVenta();
        detalle.setProducto(producto);
        detalle.setCantidad(2);

        // ↓↓↓ AGREGAR
        Sucursal sucursal = new Sucursal();
        sucursal.setId(1L);
        // ↑↑↑ AGREGAR

        venta = new Venta();
        venta.setId(1L);
        venta.setUsuario(usuario);
        venta.setSucursal(sucursal);                      
        venta.setTipoEntrega(TipoEntrega.RETIRO_TIENDA);  
        venta.setDetalles(List.of(detalle));

        lenient().when(assembler.toModel(any(Venta.class)))
                .thenAnswer(invocation -> EntityModel.of(new VentaResponseDTO()));
    }


    @Test
    void testListarVentas() throws Exception {
        when(ventaService.findAll()).thenReturn(List.of(venta));

        mockMvc.perform(get("/api/ventas"))
                .andExpect(status().isOk());
    }

    @Test
    void testObtenerVentaPorId_Existente() throws Exception {
        when(ventaService.findById(1L)).thenReturn(venta);

        mockMvc.perform(get("/api/ventas/1"))
                .andExpect(status().isOk());
    }

    @Test
    void testObtenerVentaPorId_Inexistente() throws Exception {
        when(ventaService.findById(1L)).thenReturn(null);

        mockMvc.perform(get("/api/ventas/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGuardarVenta() throws Exception {
        when(ventaService.save(any(Venta.class))).thenReturn(venta);

        mockMvc.perform(post("/api/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(venta)))
                .andExpect(status().isCreated());
    }
}