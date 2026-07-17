package com.minimarket.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(
    name = "stock_sucursal",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_stock_sucursal_producto",
        columnNames = {"sucursal_id", "producto_id"}
    )
)
@Schema(description = "Representa el stock de un producto específico en una sucursal")
public class StockSucursal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único del registro de stock", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotNull(message = "La sucursal no puede ser nula")
    @ManyToOne
    @JoinColumn(name = "sucursal_id", nullable = false)
    @Schema(description = "Sucursal a la que pertenece este stock")
    private Sucursal sucursal;

    @NotNull(message = "El producto no puede ser nulo")
    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    @Schema(description = "Producto al que corresponde este stock")
    private Producto producto;

    @Min(value = 0, message = "El stock no puede ser negativo")
    @Column(nullable = false)
    @Schema(description = "Cantidad disponible del producto en esta sucursal", example = "50")
    private Integer cantidad;

    // --- NUEVO CAMPO: umbral mínimo que dispara la reposición automática ---
    @NotNull(message = "El stock mínimo no puede ser nulo")
    @Min(value = 0, message = "El stock mínimo no puede ser negativo")
    @Column(nullable = false, name = "stock_minimo")
    @Schema(description = "Umbral mínimo de stock; bajo este valor se genera una orden de compra automática", example = "10")
    private Integer stockMinimo;

    // Optimistic locking: consistente con @Version en Producto
    @Version
    @Schema(description = "Versión para control de concurrencia optimista", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer version;

    // Getters y Setters
    public Long getId() { 
        return id; 
    }

    public void setId(Long id) { 
        this.id = id; 
    }

    public Sucursal getSucursal() { 
        return sucursal; 
    }

    public void setSucursal(Sucursal sucursal) { 
        this.sucursal = sucursal; 
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

    // --- NUEVO getter/setter ---
    public Integer getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(Integer stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public Integer getVersion() { 
        return version; 
    }

    public void setVersion(Integer version) { 
        this.version = version; 
    }
}
