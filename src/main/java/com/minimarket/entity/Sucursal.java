package com.minimarket.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Schema(description = "Representa una sucursal física del minimarket")
public class Sucursal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único de la sucursal", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "El nombre de la sucursal no puede estar vacío")
    @Column(nullable = false, unique = true)
    @Schema(description = "Nombre de la sucursal", example = "Sucursal Centro")
    private String nombre;

    @NotBlank(message = "La dirección de la sucursal no puede estar vacía")
    @Column(nullable = false)
    @Schema(description = "Dirección física de la sucursal", example = "Av. Principal 123, Santiago")
    private String direccion;

    @Schema(description = "Comuna donde se ubica la sucursal", example = "Providencia")
    private String comuna;

    @Schema(description = "Teléfono de contacto de la sucursal", example = "+56221234567")
    private String telefono;

    @Column(nullable = false)
    @Schema(description = "Indica si la sucursal está operativa (baja lógica)", example = "true")
    private Boolean activa = true;

    @OneToMany(mappedBy = "sucursal", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @Schema(description = "Lista de stocks por producto en esta sucursal")
    private List<StockSucursal> stocks;

    // Getters y Setters
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

    public List<StockSucursal> getStocks() { return stocks; }
    public void setStocks(List<StockSucursal> stocks) { this.stocks = stocks; }
}
