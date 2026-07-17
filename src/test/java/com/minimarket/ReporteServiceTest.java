package com.minimarket;

import com.minimarket.dto.RotacionProductoDTO;
import com.minimarket.repository.DetalleVentaRepository;
import com.minimarket.service.impl.ReporteServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {

    @Mock
    private DetalleVentaRepository detalleVentaRepository;

    @InjectMocks
    private ReporteServiceImpl reporteService;

    private Date ayer;
    private Date hoy;

    @BeforeEach
    void setUp() {
        Calendar cal = Calendar.getInstance();
        hoy = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, -1);
        ayer = cal.getTime();
    }

    @Test
    void testGetRotacionProductos_RetornaListaOrdenadaPorMasVendido() {
        RotacionProductoDTO masVendido = new RotacionProductoDTO(1L, "Fideos Spaghetti", 120L, 118800.0);
        RotacionProductoDTO menosVendido = new RotacionProductoDTO(2L, "Jugo de Naranja", 5L, 9000.0);

        when(detalleVentaRepository.findRotacionByPeriodo(ayer, hoy))
                .thenReturn(List.of(masVendido, menosVendido));

        List<RotacionProductoDTO> resultado = reporteService.getRotacionProductos(ayer, hoy);

        assertEquals(2, resultado.size());
        assertEquals("Fideos Spaghetti", resultado.get(0).getNombreProducto());
        assertEquals(120L, resultado.get(0).getTotalUnidadesVendidas());
    }

    @Test
    void testGetRotacionProductos_SinFechaInicio_LanzaExcepcion() {
        assertThrows(RuntimeException.class, () -> reporteService.getRotacionProductos(null, hoy));
    }

    @Test
    void testGetRotacionProductos_SinFechaFin_LanzaExcepcion() {
        assertThrows(RuntimeException.class, () -> reporteService.getRotacionProductos(ayer, null));
    }

    @Test
    void testGetRotacionProductos_FechaInicioPosteriorAFechaFin_LanzaExcepcion() {
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> reporteService.getRotacionProductos(hoy, ayer));
        assertTrue(ex.getMessage().contains("no puede ser posterior"));
    }

    @Test
    void testGetRotacionProductos_SinVentasEnElPeriodo_RetornaListaVacia() {
        when(detalleVentaRepository.findRotacionByPeriodo(any(Date.class), any(Date.class)))
                .thenReturn(List.of());

        List<RotacionProductoDTO> resultado = reporteService.getRotacionProductos(ayer, hoy);

        assertTrue(resultado.isEmpty());
    }
}
