package com.minimarket.controller;

import lombok.RequiredArgsConstructor;

import com.minimarket.assembler.CategoriaModelAssembler;
import com.minimarket.entity.Categoria;
import com.minimarket.service.CategoriaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/api/categorias", produces = { "application/hal+json", MediaType.APPLICATION_JSON_VALUE })
@Tag(name = "Categorías", description = "Gestión de las categorías de productos del minimarket.")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    private final CategoriaModelAssembler assembler;

    @Operation(summary = "Listar todas las categorías", description = "Retorna el listado completo de categorías con enlaces HATEOAS.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente",
            content = @Content(mediaType = "application/hal+json",
                array = @ArraySchema(schema = @Schema(implementation = Categoria.class)))),
        @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Sin permisos suficientes", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLEADO', 'GERENTE')")
    public CollectionModel<EntityModel<Categoria>> listarCategorias() {
        List<EntityModel<Categoria>> categorias = categoriaService.findAll().stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(categorias, linkTo(methodOn(CategoriaController.class).listarCategorias()).withSelfRel());
    }

    @Operation(summary = "Obtener categoría por ID", description = "Retorna los detalles de una categoría específica y sus enlaces.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Categoría encontrada", 
            content = @Content(mediaType = "application/hal+json", schema = @Schema(implementation = Categoria.class))),
        @ApiResponse(responseCode = "404", description = "Categoría no encontrada", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Sin permisos suficientes", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLEADO', 'GERENTE')")
    public ResponseEntity<EntityModel<Categoria>> obtenerCategoriaPorId(
            @Parameter(description = "Identificador único de la categoría", example = "1", required = true)
            @PathVariable Long id) {
        Categoria categoria = categoriaService.findById(id);
        if (categoria == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(assembler.toModel(categoria));
    }

    @Operation(summary = "Crear una nueva categoría", description = "Agrega una nueva clasificación de productos al catálogo.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Categoría creada correctamente", 
            content = @Content(mediaType = "application/hal+json", schema = @Schema(implementation = Categoria.class))),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Sin permisos suficientes (Solo GERENTE)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @PostMapping
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<EntityModel<Categoria>> guardarCategoria(
            @Parameter(description = "Datos de la categoría a registrar", required = true)
            @Valid @RequestBody Categoria categoria) {
        Categoria guardada = categoriaService.save(categoria);
        return ResponseEntity
                .created(linkTo(methodOn(CategoriaController.class).obtenerCategoriaPorId(guardada.getId())).toUri())
                .body(assembler.toModel(guardada));
    }

    @Operation(summary = "Actualizar una categoría", description = "Modifica el nombre o datos de una categoría existente.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Categoría actualizada correctamente", 
            content = @Content(mediaType = "application/hal+json", schema = @Schema(implementation = Categoria.class))),
        @ApiResponse(responseCode = "404", description = "Categoría no encontrada", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Sin permisos suficientes", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<EntityModel<Categoria>> actualizarCategoria(
            @Parameter(description = "Identificador de la categoría a actualizar", example = "1", required = true)
            @PathVariable Long id, 
            @Parameter(description = "Nuevos datos de la categoría", required = true)
            @Valid @RequestBody Categoria categoria) {
        if (categoriaService.findById(id) != null) {
            categoria.setId(id);
            return ResponseEntity.ok(assembler.toModel(categoriaService.save(categoria)));
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "Eliminar una categoría", description = "Elimina de forma permanente una categoría del catálogo.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Categoría eliminada con éxito"), // Sin @Content
        @ApiResponse(responseCode = "404", description = "Categoría no encontrada", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Sin permisos suficientes", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<Void> eliminarCategoria(
            @Parameter(description = "Identificador de la categoría a eliminar", example = "1", required = true)
            @PathVariable Long id) {
        if (categoriaService.findById(id) != null) {
            categoriaService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}