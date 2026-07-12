package com.minimarket.entity;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuarios")
@Schema(description = "Representa a un usuario del sistema (Gerente, Empleado o Cliente)")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único del usuario", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(nullable = false, unique = true)
    @Schema(description = "Nombre de usuario utilizado para iniciar sesión", example = "gerente")
    private String username;

    @Column(nullable = false)
    @Schema(description = "Nombre del usuario", example = "Francisca")
    private String nombre;

    @Column(nullable = false)
    @Schema(description = "Apellido del usuario", example = "Valenzuela")
    private String apellido;

    @Column(nullable = false, unique = true)
    @Schema(description = "Correo electrónico del usuario", example = "francisca.valenzuela@minimarket.cl")
    private String email;

    @Column(nullable = false)
    @JsonIgnore
    @Schema(description = "Contraseña encriptada con BCrypt. Nunca se expone en las respuestas.", accessMode = Schema.AccessMode.WRITE_ONLY)
    private String password;

    @Column
    @Schema(description = "Dirección de despacho o residencia", example = "Av. Siempre Viva 742, Santiago")
    private String direccion;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "usuario_roles",
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "rol_id")
    )
    @Schema(description = "Roles asignados al usuario (ej. ROLE_GERENTE, ROLE_EMPLEADO, ROLE_CLIENTE)")
    private Set<Rol> roles;

    // Getters y Setters
    public Long getId() { 
        return id; 
        
    }

    public void setId(Long id) { 
        this.id = id; 
    }

    public String getUsername() { 
        return username; 
    }

    public void setUsername(String username) { 
        this.username = username; 
    }

    public String getPassword() { 
        return password; 
    }
    public void setPassword(String password) { 
        this.password = password; 
    }

    public String getNombre() { 
        return nombre; 
    }

    public void setNombre(String nombre) { 
        this.nombre = nombre; 
    }

    public String getApellido() { 
        return apellido; 
    }

    public void setApellido(String apellido) { 
        this.apellido = apellido; 
    }

    public String getEmail() { 
        return email; 
    }

    public void setEmail(String email) { 
        this.email = email; 
    }

    public String getDireccion() { 
        return direccion; 
    }

    public void setDireccion(String direccion) { 
        this.direccion = direccion; 
    }

    public Set<Rol> getRoles() { 
        return roles; 
    }
    public void setRoles(Set<Rol> roles) { 
        this.roles = roles; 
    }
}