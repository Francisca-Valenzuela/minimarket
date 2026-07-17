package com.minimarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resumen liviano de sucursal, sin lista de stocks, para uso en órdenes de compra")
public class SucursalResumenDTO {

    private Long id;
    private String nombre;
    private String direccion;

    public SucursalResumenDTO(Long id, String nombre, String direccion) {
        this.id = id;
        this.nombre = nombre;
        this.direccion = direccion;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDireccion() { return direccion; }
}
