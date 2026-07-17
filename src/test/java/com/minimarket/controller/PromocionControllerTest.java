package com.minimarket.controller;

import com.minimarket.dto.PromocionRequestDTO;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Promocion;
import com.minimarket.entity.Sucursal;
import com.minimarket.entity.TipoDescuento;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.PromocionRepository;
import com.minimarket.repository.SucursalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PromocionControllerTest {

    @Mock
    private PromocionRepository promocionRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private SucursalRepository sucursalRepository;

    @InjectMocks
    private PromocionController promocionController;

    private Producto producto;
    private Sucursal sucursal;

    @BeforeEach
    void setUp() {
        producto = new Producto();
        producto.setId(10L);
        producto.setNombre("Leche Entera 1L");
        producto.setPrecio(1200.0);

        sucursal = new Sucursal();
        sucursal.setId(1L);
        sucursal.setNombre("Sucursal Centro");
        sucursal.setDireccion("Av. Principal 123");
    }

    @Test
    void testCrear_PromocionGlobal_SinSucursal() {
        PromocionRequestDTO request = new PromocionRequestDTO();
        request.setProductoId(10L);
        request.setSucursalId(null);
        request.setTipoDescuento("PORCENTAJE");
        request.setValor(15.0);
        request.setFechaInicio(LocalDate.now());
        request.setFechaFin(LocalDate.now().plusDays(30));

        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(promocionRepository.save(any(Promocion.class))).thenAnswer(inv -> {
            Promocion p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        var response = promocionController.crear(request);

        assertEquals(200, response.getStatusCode().value());
        assertNull(response.getBody().getSucursal());
        assertEquals("PORCENTAJE", response.getBody().getTipoDescuento());
        verify(sucursalRepository, never()).findById(any());
    }

    @Test
    void testCrear_PromocionPorSucursal() {
        PromocionRequestDTO request = new PromocionRequestDTO();
        request.setProductoId(10L);
        request.setSucursalId(1L);
        request.setTipoDescuento("MONTO_FIJO");
        request.setValor(200.0);
        request.setFechaInicio(LocalDate.now());
        request.setFechaFin(LocalDate.now().plusDays(10));

        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(sucursalRepository.findById(1L)).thenReturn(Optional.of(sucursal));
        when(promocionRepository.save(any(Promocion.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = promocionController.crear(request);

        assertNotNull(response.getBody().getSucursal());
        assertEquals("Sucursal Centro", response.getBody().getSucursal().getNombre());
        assertEquals(TipoDescuento.MONTO_FIJO.name(), response.getBody().getTipoDescuento());
    }

    @Test
    void testCrear_ProductoInexistente_LanzaExcepcion() {
        PromocionRequestDTO request = new PromocionRequestDTO();
        request.setProductoId(99L);
        request.setTipoDescuento("PORCENTAJE");
        request.setValor(10.0);
        request.setFechaInicio(LocalDate.now());
        request.setFechaFin(LocalDate.now().plusDays(1));

        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> promocionController.crear(request));
        verify(promocionRepository, never()).save(any());
    }

    @Test
    void testListar() {
        Promocion promo = construirPromocion(1L, null);
        when(promocionRepository.findAll()).thenReturn(List.of(promo));

        var response = promocionController.listar();

        assertEquals(1, response.getBody().size());
    }

    @Test
    void testObtener_NoExistente_LanzaExcepcion() {
        when(promocionRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> promocionController.obtener(99L));
    }

    @Test
    void testDesactivar_MarcaActivaFalse() {
        Promocion promo = construirPromocion(1L, null);
        when(promocionRepository.findById(1L)).thenReturn(Optional.of(promo));
        when(promocionRepository.save(any(Promocion.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = promocionController.desactivar(1L);

        assertFalse(response.getBody().getActiva());
    }

    @Test
    void testEliminar() {
        doNothing().when(promocionRepository).deleteById(1L);

        var response = promocionController.eliminar(1L);

        assertEquals(204, response.getStatusCode().value());
        verify(promocionRepository, times(1)).deleteById(1L);
    }

    private Promocion construirPromocion(Long id, Sucursal sucursalAsociada) {
        Promocion promo = new Promocion();
        promo.setId(id);
        promo.setProducto(producto);
        promo.setSucursal(sucursalAsociada);
        promo.setTipoDescuento(TipoDescuento.PORCENTAJE);
        promo.setValor(10.0);
        promo.setFechaInicio(LocalDate.now());
        promo.setFechaFin(LocalDate.now().plusDays(5));
        promo.setActiva(true);
        return promo;
    }
}
