package com.minimarket.service.impl;

import com.minimarket.dto.RotacionProductoDTO;
import com.minimarket.repository.DetalleVentaRepository;
import com.minimarket.service.ReporteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.List;

@Service
public class ReporteServiceImpl implements ReporteService {

    private final DetalleVentaRepository detalleVentaRepository;

    public ReporteServiceImpl(DetalleVentaRepository detalleVentaRepository) {
        this.detalleVentaRepository = detalleVentaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RotacionProductoDTO> getRotacionProductos(Date fechaInicio, Date fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            throw new RuntimeException("Las fechas de inicio y fin son obligatorias");
        }
        if (fechaInicio.after(fechaFin)) {
            throw new RuntimeException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }
        return detalleVentaRepository.findRotacionByPeriodo(fechaInicio, fechaFin);
    }
}