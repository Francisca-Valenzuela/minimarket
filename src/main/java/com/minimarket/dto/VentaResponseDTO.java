package com.minimarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import java.util.List;

import com.minimarket.entity.TipoEntrega;

@Schema(description = "Objeto de transferencia que representa una Venta, sin exponer datos internos de la BD")
public class VentaResponseDTO {

    @Schema(description = "ID de la venta", example = "1")
    private Long id;

    @Schema(description = "Fecha en la que se realizó la transacción")
    private Date fecha;

    @Schema(description = "Monto total de la venta", example = "2400.0")
    private Double total;

    @Schema(description = "Nombre completo del comprador", example = "Pedro Soto")
    private String comprador;

    // --- NUEVO CAMPO: nombre de la sucursal donde se realizó la venta ---
    @Schema(description = "Nombre de la sucursal donde se realizó la venta", example = "Sucursal Providencia")
    private String sucursal;

    @Schema(description = "Lista plana de los productos comprados")
    private List<DetalleVentaResponseDTO> detalles;

    @Schema(description = "Modalidad de entrega de la venta", example = "DESPACHO_DOMICILIO")
    private TipoEntrega tipoEntrega;

    @Schema(description = "Dirección de despacho (null si es retiro en tienda)", example = "Av. Siempre Viva 742, Santiago")
    private String direccionDespacho;

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public String getComprador() {
        return comprador;
    }

    public void setComprador(String comprador) {
        this.comprador = comprador;
    }

    // --- NUEVO getter/setter ---
    public String getSucursal() {
        return sucursal;
    }

    public void setSucursal(String sucursal) {
        this.sucursal = sucursal;
    }

    public List<DetalleVentaResponseDTO> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleVentaResponseDTO> detalles) {
        this.detalles = detalles;
    }

    public TipoEntrega getTipoEntrega() {
        return tipoEntrega;
    }

    public void setTipoEntrega(TipoEntrega tipoEntrega) {
        this.tipoEntrega = tipoEntrega;
    }

    public String getDireccionDespacho() {
        return direccionDespacho;
    }

    public void setDireccionDespacho(String direccionDespacho) {
        this.direccionDespacho = direccionDespacho;
    }
}
