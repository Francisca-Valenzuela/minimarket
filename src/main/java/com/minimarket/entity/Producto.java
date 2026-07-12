package com.minimarket.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

@Entity
@Schema(description = "Entidad que representa un producto disponible para la venta")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único del producto", example = "3")
    private Long id;

    @Column(nullable = false)
    @Schema(description = "Nombre comercial del producto", example = "Leche Entera 1L")
    private String nombre;

    @Column(nullable = false)
    @Schema(description = "Precio unitario del producto en pesos", example = "1200.0")
    private Double precio;

    @Column(nullable = false)
    @Schema(description = "Cantidad de unidades disponibles en el inventario", example = "50")
    private Integer stock;

    @Version
    @Schema(description = "Control de concurrencia optimista (uso interno)", example = "0")
    private Integer version;

    @ManyToOne
    @JoinColumn(name = "categoria_id", nullable = false)
    @Schema(description = "Categoría a la cual pertenece el producto")
    private Categoria categoria;

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) { 
        this.version = version; 
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }
}