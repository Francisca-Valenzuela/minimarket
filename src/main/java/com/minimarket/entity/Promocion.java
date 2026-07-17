package com.minimarket.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "promociones")
@Schema(description = "Promoción aplicable a un producto, de forma global o restringida a una sucursal")
public class Promocion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID de la promoción", example = "1")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    @Schema(description = "Producto al cual aplica la promoción")
    private Producto producto;

    @ManyToOne(optional = true)
    @JoinColumn(name = "sucursal_id", nullable = true)
    @Schema(description = "Sucursal donde aplica. Si es null, la promoción es global")
    private Sucursal sucursal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(description = "Tipo de descuento: PORCENTAJE o MONTO_FIJO", example = "PORCENTAJE")
    private TipoDescuento tipoDescuento;

    @Column(nullable = false)
    @Schema(description = "Valor del descuento (0-100 si PORCENTAJE; en pesos si MONTO_FIJO)", example = "15.0")
    private Double valor;

    @Column(nullable = false)
    @Schema(description = "Fecha de inicio de la promoción", example = "2026-07-01")
    private LocalDate fechaInicio;

    @Column(nullable = false)
    @Schema(description = "Fecha de término de la promoción (inclusive)", example = "2026-07-31")
    private LocalDate fechaFin;

    @Column(nullable = false)
    @Schema(description = "Indica si la promoción está activa manualmente", example = "true")
    private Boolean activa = true;

    public Promocion() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }
    public Sucursal getSucursal() { return sucursal; }
    public void setSucursal(Sucursal sucursal) { this.sucursal = sucursal; }
    public TipoDescuento getTipoDescuento() { return tipoDescuento; }
    public void setTipoDescuento(TipoDescuento tipoDescuento) { this.tipoDescuento = tipoDescuento; }
    public Double getValor() { return valor; }
    public void setValor(Double valor) { this.valor = valor; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }
    public Boolean getActiva() { return activa; }
    public void setActiva(Boolean activa) { this.activa = activa; }

    /** Regla de negocio: ¿esta promo está vigente hoy, para esta sucursal? */
    public boolean esVigentePara(Long sucursalId, LocalDate fecha) {
        boolean dentroDeFecha = !fecha.isBefore(fechaInicio) && !fecha.isAfter(fechaFin);
        boolean aplicaSucursal = (sucursal == null) || sucursal.getId().equals(sucursalId);
        return activa && dentroDeFecha && aplicaSucursal;
    }
}
