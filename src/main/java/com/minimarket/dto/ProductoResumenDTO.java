package com.minimarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resumen del producto a reponer en una orden de compra")
public class ProductoResumenDTO {

    private Long id;
    private String nombre;
    private Double precio;

    public ProductoResumenDTO(Long id, String nombre, Double precio) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public Double getPrecio() { return precio; }
}
