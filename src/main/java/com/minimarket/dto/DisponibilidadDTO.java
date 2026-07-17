package com.minimarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class DisponibilidadDTO {

    @Schema(description = "ID de la sucursal", example = "1")
    private Long sucursalId;

    @Schema(description = "Nombre de la sucursal", example = "Sucursal Providencia")
    private String sucursalNombre;

    @Schema(description = "ID del producto", example = "5")
    private Long productoId;

    @Schema(description = "Nombre del producto", example = "Leche Entera 1L")
    private String productoNombre;

    @Schema(description = "Cantidad disponible", example = "35")
    private Integer cantidad;

    @Schema(description = "¿Hay stock disponible?", example = "true")
    private Boolean disponible;

    public DisponibilidadDTO() {}

    public Long getSucursalId() { return sucursalId; }
    public void setSucursalId(Long sucursalId) { this.sucursalId = sucursalId; }
    public String getSucursalNombre() { return sucursalNombre; }
    public void setSucursalNombre(String sucursalNombre) { this.sucursalNombre = sucursalNombre; }
    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }
    public String getProductoNombre() { return productoNombre; }
    public void setProductoNombre(String productoNombre) { this.productoNombre = productoNombre; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public Boolean getDisponible() { return disponible; }
    public void setDisponible(Boolean disponible) { this.disponible = disponible; }
}