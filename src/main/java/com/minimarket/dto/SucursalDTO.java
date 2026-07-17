package com.minimarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public class SucursalDTO {

    @Schema(description = "ID de la sucursal", example = "1")
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Schema(description = "Nombre", example = "Sucursal Maipú")
    private String nombre;

    @NotBlank(message = "La dirección es obligatoria")
    @Schema(description = "Dirección", example = "Av. Pajaritos 5000")
    private String direccion;

    @NotBlank(message = "La comuna es obligatoria")
    @Schema(description = "Comuna", example = "Maipú")
    private String comuna;

    @Schema(description = "Teléfono", example = "+56229876543")
    private String telefono;

    @Schema(description = "Sucursal activa", example = "true")
    private Boolean activa;

    public SucursalDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getComuna() { return comuna; }
    public void setComuna(String comuna) { this.comuna = comuna; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public Boolean getActiva() { return activa; }
    public void setActiva(Boolean activa) { this.activa = activa; }
}