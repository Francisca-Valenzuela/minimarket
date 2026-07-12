package com.minimarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Objeto de transferencia de datos para crear o actualizar un producto")
public class ProductoDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Schema(description = "Nombre comercial del producto", example = "Café en grano 250g")
    private String nombre;

    @NotNull(message = "El precio es obligatorio")
    @Min(value = 0, message = "El precio debe ser positivo")
    @Schema(description = "Precio unitario del producto", example = "4500.0")
    private Double precio;

    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock debe ser positivo")
    @Schema(description = "Stock inicial o actual del producto", example = "20")
    private Integer stock;

    @NotNull(message = "La categoría es obligatoria")
    @Schema(description = "ID de la categoría a la que pertenece el producto", example = "1")
    private Long categoriaId;
    
    // --- GETTERS Y SETTERS MANUALES ---

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

    public Long getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Long categoriaId) {
        this.categoriaId = categoriaId;
    }
}