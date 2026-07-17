package com.minimarket.dto;

import java.time.LocalDate;

public class PromocionResponseDTO {
    private Long id;
    private ProductoResumenDTO producto;
    private SucursalResumenDTO sucursal; // null si es global
    private String tipoDescuento;
    private Double valor;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Boolean activa;

    public PromocionResponseDTO(Long id, ProductoResumenDTO producto, SucursalResumenDTO sucursal,
                                  String tipoDescuento, Double valor, LocalDate fechaInicio,
                                  LocalDate fechaFin, Boolean activa) {
        this.id = id;
        this.producto = producto;
        this.sucursal = sucursal;
        this.tipoDescuento = tipoDescuento;
        this.valor = valor;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.activa = activa;
    }

    public Long getId() { return id; }
    public ProductoResumenDTO getProducto() { return producto; }
    public SucursalResumenDTO getSucursal() { return sucursal; }
    public String getTipoDescuento() { return tipoDescuento; }
    public Double getValor() { return valor; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public Boolean getActiva() { return activa; }
}
