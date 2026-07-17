package com.minimarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resumen del proveedor asociado a una orden de compra")
public class ProveedorResumenDTO {

    private Long id;
    private String nombre;
    private String rut;
    private String email;
    private String telefono;

    public ProveedorResumenDTO(Long id, String nombre, String rut, String email, String telefono) {
        this.id = id;
        this.nombre = nombre;
        this.rut = rut;
        this.email = email;
        this.telefono = telefono;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getRut() { return rut; }
    public String getEmail() { return email; }
    public String getTelefono() { return telefono; }
}
