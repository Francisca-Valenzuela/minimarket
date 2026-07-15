package com.minimarket.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Schema(description = "Representa un movimiento de stock (entrada o salida) para un producto")
public class Inventario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único del movimiento de inventario", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotNull(message = "El movimiento debe estar asociado a un producto")
    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    @Schema(description = "Producto asociado al movimiento de inventario")
    private Producto producto;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor a cero")
    @Column(nullable = false)
    @Schema(description = "Cantidad de unidades involucradas en el movimiento", example = "20")
    private Integer cantidad;

    @NotBlank(message = "El tipo de movimiento es obligatorio")
    @Column(nullable = false)
    @Schema(description = "Tipo de movimiento de inventario", example = "Entrada", allowableValues = {"Entrada", "Salida"})
    private String tipoMovimiento; // Ejemplo: "Entrada" o "Salida"

    @NotNull(message = "La fecha del movimiento es obligatoria")
    @Column(nullable = false)
    @Schema(description = "Fecha y hora en que se registró el movimiento", example = "2026-07-08T09:15:00.000Z")
    private Date fechaMovimiento;

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public String getTipoMovimiento() {
        return tipoMovimiento;
    }

    public void setTipoMovimiento(String tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
    }

    public Date getFechaMovimiento() {
        return fechaMovimiento;
    }

    public void setFechaMovimiento(Date fechaMovimiento) {
        this.fechaMovimiento = fechaMovimiento;
    }
}