package com.minimarket.service;

import com.minimarket.dto.RotacionProductoDTO;
import java.util.Date;
import java.util.List;

public interface ReporteService {
    List<RotacionProductoDTO> getRotacionProductos(Date fechaInicio, Date fechaFin);
}
