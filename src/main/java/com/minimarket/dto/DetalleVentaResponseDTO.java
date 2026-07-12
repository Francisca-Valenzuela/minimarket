package com.minimarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Objeto que representa un ítem dentro de la venta (Respuesta limpia)")
public class DetalleVentaResponseDTO {
    
    @Schema(description = "ID del detalle", example = "1")
    private Long id;
    
    @Schema(description = "Nombre del producto comprado", example = "Leche Entera 1L")
    private String producto;
    
    @Schema(description = "Cantidad comprada", example = "2")
    private Integer cantidad;
    
    @Schema(description = "Precio unitario cobrado en esta venta", example = "1200.0")
    private Double precio;

    // Getters y Setters
    public Long getId() { 
        return id; 
    }

    public void setId(Long id) { 
        this.id = id; 
    }

    public String getProducto() { 
        return producto; 
    }

    public void setProducto(String producto) { 
        this.producto = producto; 
    }

    public Integer getCantidad() { 
        return cantidad; 
    }

    public void setCantidad(Integer cantidad) { 
        this.cantidad = cantidad; 
    }

    public Double getPrecio() { 
        return precio; 
    }

    public void setPrecio(Double precio) { 
        this.precio = precio; 
    }
}