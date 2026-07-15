package com.minimarket.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

@Entity
@Schema(description = "Entidad que representa una categoría de productos en el catálogo")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único de la categoría", example = "1")
    private Long id;

    @NotBlank(message = "El nombre de la categoría es obligatorio")
    @Column(nullable = false, unique = true)
    @Schema(description = "Nombre descriptivo de la categoría", example = "Abarrotes")
    private String nombre;

    // Sin cascade: eliminar una categoría NO debe eliminar sus productos de forma
    // silenciosa. CategoriaServiceImpl.deleteById() valida explícitamente que la
    // categoría no tenga productos asociados antes de permitir el borrado.
    @OneToMany(mappedBy = "categoria")
    @JsonIgnore
    @Schema(description = "Lista de productos que pertenecen a esta categoría", hidden = true)
    private List<Producto> productos;

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

    public List<Producto> getProductos() {
        return productos;
    }

    public void setProductos(List<Producto> productos) {
        this.productos = productos;
    }
}
