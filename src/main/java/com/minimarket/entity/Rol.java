package com.minimarket.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.util.Set;

@Entity
@Schema(description = "Entidad que representa un rol de seguridad en el sistema")
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único del rol", example = "3")
    private Long id;

    @Column(nullable = false, unique = true)
    @Schema(description = "Nombre del rol (debe iniciar obligatoriamente con ROLE_)", example = "ROLE_CLIENTE")
    private String nombre;

    @ManyToMany(mappedBy = "roles")
    @Schema(hidden = true) // Lo ocultamos para que Swagger no haga un bucle infinito mostrando a los usuarios
    private Set<Usuario> usuarios;

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Set<Usuario> getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(Set<Usuario> usuarios) {
        this.usuarios = usuarios;
    }
}