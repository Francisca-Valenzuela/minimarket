package com.minimarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Objeto de transferencia que representa un ítem de carrito, sin exponer los datos completos del usuario")
public class CarritoResponseDTO {

    @Schema(description = "ID del ítem de carrito", example = "1")
    private Long id;

    @Schema(description = "Nombre completo del dueño del carrito", example = "Pedro Soto")
    private String usuario;

    @Schema(description = "Nombre del producto agregado", example = "Leche Entera 1L")
    private String producto;

    @Schema(description = "Cantidad del producto en el carrito", example = "2")
    private Integer cantidad;

    // Getters y Setters
    public Long getId() { 
        return id; 
    }

    public void setId(Long id) { 
        this.id = id; 
    }

    public String getUsuario() { 
        return usuario; }

    public void setUsuario(String usuario) { 
        this.usuario = usuario; 
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
}