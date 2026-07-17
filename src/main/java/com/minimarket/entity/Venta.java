package com.minimarket.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Entity
@Schema(description = "Representa una venta realizada a un usuario, con el detalle de productos vendidos")
public class Venta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único de la venta", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotNull(message = "La venta debe estar asociada a un usuario")
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    @Schema(description = "Usuario (cliente) al que pertenece la venta")
    private Usuario usuario;

    // --- NUEVO CAMPO: requerido para descontar stock desde la sucursal correcta ---
    @NotNull(message = "La venta debe estar asociada a una sucursal")
    @ManyToOne
    @JoinColumn(name = "sucursal_id", nullable = false)
    @Schema(description = "Sucursal en la que se realizó la venta (define de dónde se descuenta el stock)")
    private Sucursal sucursal;

    @Column(nullable = false)
    @Schema(description = "Fecha en que se registró la venta", example = "2026-07-08T14:30:00.000Z")
    private Date fecha;

    @NotEmpty(message = "La venta debe contener al menos un detalle")
    @Valid
    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL)
    @Schema(description = "Listado de productos y cantidades incluidos en la venta")
    private List<DetalleVenta> detalles;

    // Campo persistente para el total de la venta.
    // Se calcula y asigna en VentaServiceImpl.save() a partir de los detalles,
    // evitando recalcular la suma en cada punto del flujo (trazabilidad).
    @Column(nullable = true)
    @Schema(description = "Monto total de la venta, calculado a partir de la suma de los detalles", example = "15990.0", accessMode = Schema.AccessMode.READ_ONLY)
    private Double total;

    @NotNull(message = "Debe indicar el tipo de entrega (RETIRO_TIENDA o DESPACHO_DOMICILIO)")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(description = "Modalidad de entrega de la venta", example = "DESPACHO_DOMICILIO")
    private TipoEntrega tipoEntrega;

    @Column(nullable = true)
    @Schema(description = "Dirección de despacho (obligatoria solo si tipoEntrega es DESPACHO_DOMICILIO)",
            example = "Av. Siempre Viva 742, Santiago")
    private String direccionDespacho;

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

    // --- NUEVO getter/setter ---
    public Sucursal getSucursal() {
        return sucursal;
    }

    public void setSucursal(Sucursal sucursal) {
        this.sucursal = sucursal;
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
