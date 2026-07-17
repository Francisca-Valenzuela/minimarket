package com.minimarket.controller;

import com.minimarket.dto.RotacionProductoDTO;
import com.minimarket.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/reportes")
@Tag(name = "Reportes", description = "Endpoints para reportes gerenciales y análisis de ventas")
@SecurityRequirement(name = "bearerAuth")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    /**
     * Solo GERENTE puede acceder a reportes de rotación.
     * Fechas recibidas como query params en formato ISO: ?fechaInicio=2026-01-01&fechaFin=2026-07-16
     */
    @GetMapping("/rotacion")
    @PreAuthorize("hasRole('GERENTE')")
    @Operation(
        summary = "Reporte de rotación de productos",
        description = "Devuelve los productos ordenados por unidades vendidas en el período indicado. Solo accesible por GERENTE."
    )
    public ResponseEntity<List<RotacionProductoDTO>> getRotacion(
        @Parameter(description = "Fecha de inicio del período (yyyy-MM-dd)", example = "2026-01-01")
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fechaInicio,

        @Parameter(description = "Fecha de fin del período (yyyy-MM-dd)", example = "2026-07-16")
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date fechaFin
    ) {
        List<RotacionProductoDTO> resultado = reporteService.getRotacionProductos(fechaInicio, fechaFin);
        return ResponseEntity.ok(resultado);
    }
}