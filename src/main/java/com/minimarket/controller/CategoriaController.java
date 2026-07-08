package com.minimarket.controller;

import com.minimarket.assembler.CategoriaModelAssembler;
import com.minimarket.entity.Categoria;
import com.minimarket.service.CategoriaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/categorias")
@Tag(name = "Categorías", description = "Gestión de las categorías de productos del minimarket.")
@SecurityRequirement(name = "bearerAuth")
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private CategoriaModelAssembler assembler;

    @Operation(summary = "Listar todas las categorías", description = "Retorna el listado completo de categorías con enlaces HATEOAS.")
    @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente")
    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLEADO', 'GERENTE')")
    public CollectionModel<EntityModel<Categoria>> listarCategorias() {
        List<EntityModel<Categoria>> categorias = categoriaService.findAll().stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(categorias, linkTo(methodOn(CategoriaController.class).listarCategorias()).withSelfRel());
    }

    @Operation(summary = "Obtener categoría por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Categoría encontrada"),
        @ApiResponse(responseCode = "404", description = "Categoría no encontrada", content = @Content)
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLEADO', 'GERENTE')")
    public ResponseEntity<EntityModel<Categoria>> obtenerCategoriaPorId(@PathVariable Long id) {
        Categoria categoria = categoriaService.findById(id);
        if (categoria == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(assembler.toModel(categoria));
    }

    @Operation(summary = "Crear una nueva categoría")
    @ApiResponse(responseCode = "201", description = "Categoría creada correctamente")
    @PostMapping
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<EntityModel<Categoria>> guardarCategoria(@Valid @RequestBody Categoria categoria) {
        Categoria guardada = categoriaService.save(categoria);
        return ResponseEntity
                .created(linkTo(methodOn(CategoriaController.class).obtenerCategoriaPorId(guardada.getId())).toUri())
                .body(assembler.toModel(guardada));
    }

    @Operation(summary = "Actualizar una categoría existente")
    @ApiResponse(responseCode = "200", description = "Categoría actualizada")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<EntityModel<Categoria>> actualizarCategoria(@PathVariable Long id, @Valid @RequestBody Categoria categoria) {
        if (categoriaService.findById(id) != null) {
            categoria.setId(id);
            return ResponseEntity.ok(assembler.toModel(categoriaService.save(categoria)));
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "Eliminar una categoría")
    @ApiResponse(responseCode = "204", description = "Categoría eliminada", content = @Content)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<Void> eliminarCategoria(@PathVariable Long id) {
        if (categoriaService.findById(id) != null) {
            categoriaService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}