package com.minimarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO que representa la rotación de un producto en un período de tiempo")
public class RotacionProductoDTO {

    @Schema(description = "Identificador del producto", example = "3")
    private Long productoId;

    @Schema(description = "Nombre del producto", example = "Leche Entera 1L")
    private String nombreProducto;

    @Schema(description = "Total de unidades vendidas en el período", example = "120")
    private Long totalUnidadesVendidas;

    @Schema(description = "Ingresos totales generados por el producto en el período", example = "95880.0")
    private Double totalIngresos;

    // Constructor usado por la query JPQL
    public RotacionProductoDTO(Long productoId, String nombreProducto,
                                Long totalUnidadesVendidas, Double totalIngresos) {
        this.productoId = productoId;
        this.nombreProducto = nombreProducto;
        this.totalUnidadesVendidas = totalUnidadesVendidas;
        this.totalIngresos = totalIngresos;
    }

    // Getters
    public Long getProductoId() { 
        return productoId; 
    }
    
    public String getNombreProducto() { return nombreProducto; }
    public Long getTotalUnidadesVendidas() { return totalUnidadesVendidas; }
    public Double getTotalIngresos() { return totalIngresos; }

    // Setters
    public void setProductoId(Long productoId) { this.productoId = productoId; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }
    public void setTotalUnidadesVendidas(Long totalUnidadesVendidas) { this.totalUnidadesVendidas = totalUnidadesVendidas; }
    public void setTotalIngresos(Double totalIngresos) { this.totalIngresos = totalIngresos; }
}