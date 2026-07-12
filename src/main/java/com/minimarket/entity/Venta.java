package com.minimarket.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Schema(description = "Representa una venta realizada a un usuario, con el detalle de productos vendidos")
public class Venta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único de la venta", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    @Schema(description = "Usuario (cliente) al que pertenece la venta")
    private Usuario usuario;

    @Column(nullable = false)
    @Schema(description = "Fecha en que se registró la venta", example = "2026-07-08T14:30:00.000Z")
    private Date fecha;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL)
    @Schema(description = "Listado de productos y cantidades incluidos en la venta")
    private List<DetalleVenta> detalles;

    // Campo persistente para el total de la venta.
    // Se calcula y asigna en VentaServiceImpl.save() a partir de los detalles,
    // evitando recalcular la suma en cada punto del flujo (trazabilidad).
    @Column(nullable = true)
    @Schema(description = "Monto total de la venta, calculado a partir de la suma de los detalles", example = "15990.0", accessMode = Schema.AccessMode.READ_ONLY)
    private Double total;

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public List<DetalleVenta> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleVenta> detalles) {
        this.detalles = detalles;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }
}