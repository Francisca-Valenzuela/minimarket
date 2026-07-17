package com.minimarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resumen del stock de un producto en una sucursal, sin referencia circular")
public class StockSucursalResumenDTO {

    private Long id;
    private Long productoId;
    private String productoNombre;
    private Integer cantidad;
    private Integer stockMinimo;

    public StockSucursalResumenDTO(Long id, Long productoId, String productoNombre,
                                     Integer cantidad, Integer stockMinimo) {
        this.id = id;
        this.productoId = productoId;
        this.productoNombre = productoNombre;
        this.cantidad = cantidad;
        this.stockMinimo = stockMinimo;
    }

    public Long getId() { return id; }
    public Long getProductoId() { return productoId; }
    public String getProductoNombre() { return productoNombre; }
    public Integer getCantidad() { return cantidad; }
    public Integer getStockMinimo() { return stockMinimo; }
}
