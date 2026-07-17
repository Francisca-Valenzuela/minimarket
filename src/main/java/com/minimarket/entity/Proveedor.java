package com.minimarket.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "proveedores")
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID del proveedor", example = "1")
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Column(nullable = false)
    @Schema(description = "Razón social", example = "Distribuidora Andina SpA")
    private String nombre;

    @NotBlank(message = "El RUT es obligatorio")
    @Column(nullable = false, unique = true)
    @Schema(description = "RUT del proveedor", example = "76.123.456-7")
    private String rut;

    @Email(message = "Email inválido")
    @Schema(description = "Email de contacto", example = "ventas@andina.cl")
    private String email;

    @Schema(description = "Teléfono", example = "+56223334444")
    private String telefono;

    @Column(nullable = false)
    @Schema(description = "Proveedor activo", example = "true")
    private Boolean activo = true;

    public Proveedor() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getRut() { return rut; }
    public void setRut(String rut) { this.rut = rut; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}