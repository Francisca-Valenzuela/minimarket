package com.minimarket.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ordenes_compra")
public class OrdenCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID de la orden de compra", example = "1")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "proveedor_id", nullable = false)
    @Schema(description = "Proveedor destinatario")
    private Proveedor proveedor;

    @ManyToOne(optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    @Schema(description = "Producto a reponer")
    private Producto producto;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sucursal_id", nullable = false)
    @Schema(description = "Sucursal que requiere la reposición")
    private Sucursal sucursal;

    @Column(nullable = false)
    @Schema(description = "Cantidad solicitada", example = "100")
    private Integer cantidadSolicitada;

    @Column(nullable = false)
    @Schema(description = "Fecha de creación de la orden")
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(description = "Estado de la orden", example = "PENDIENTE")
    private EstadoOrdenCompra estado = EstadoOrdenCompra.PENDIENTE;

    @Column(nullable = false)
    @Schema(description = "true si fue generada automáticamente por bajo stock", example = "true")
    private Boolean automatica = false;

    public OrdenCompra() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Proveedor getProveedor() { return proveedor; }
    public void setProveedor(Proveedor proveedor) { this.proveedor = proveedor; }
    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }
    public Sucursal getSucursal() { return sucursal; }
    public void setSucursal(Sucursal sucursal) { this.sucursal = sucursal; }
    public Integer getCantidadSolicitada() { return cantidadSolicitada; }
    public void setCantidadSolicitada(Integer cantidadSolicitada) { this.cantidadSolicitada = cantidadSolicitada; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public EstadoOrdenCompra getEstado() { return estado; }
    public void setEstado(EstadoOrdenCompra estado) { this.estado = estado; }
    public Boolean getAutomatica() { return automatica; }
    public void setAutomatica(Boolean automatica) { this.automatica = automatica; }
}