package com.minimarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Datos requeridos para crear o actualizar un producto")
public class ProductoDTO {

    @Schema(description = "Nombre del producto", example = "Arroz Grado 1 1kg")
    @NotBlank(message = "El nombre del producto no puede estar vacío")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    @Pattern(regexp = "^[^<>\"'%;()&+]*$", message = "El nombre contiene caracteres no permitidos")
    private String nombre;

    @Schema(description = "Precio unitario en pesos chilenos", example = "1490")
    @NotNull(message = "El precio es obligatorio")
    @Min(value = 0, message = "El precio no puede ser negativo")
    private Double precio;

    @Schema(description = "Cantidad disponible en stock", example = "50")
    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;

    @Schema(description = "ID de la categoría a la que pertenece el producto", example = "1")
    @NotNull(message = "La categoría es obligatoria")
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