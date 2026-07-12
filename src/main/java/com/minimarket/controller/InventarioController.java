package com.minimarket.controller;

import com.minimarket.assembler.InventarioModelAssembler;
import com.minimarket.entity.Inventario;
import com.minimarket.service.InventarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping(value = "/api/inventario", produces = { "application/hal+json", MediaType.APPLICATION_JSON_VALUE })
@Tag(name = "Inventario", description = "Gestión de los niveles de stock e inventario de productos.")
@SecurityRequirement(name = "bearerAuth")
public class InventarioController {

    @Autowired
    private InventarioService inventarioService;

    @Autowired
    private InventarioModelAssembler assembler;

    @Operation(summary = "Listar inventario")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente",
            content = @Content(mediaType = "application/hal+json",
                array = @ArraySchema(schema = @Schema(implementation = Inventario.class)))),
        @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Sin permisos suficientes", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLEADO', 'GERENTE')")
    public CollectionModel<EntityModel<Inventario>> listarInventario() {
        List<EntityModel<Inventario>> inventarios = inventarioService.findAll().stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(inventarios, linkTo(methodOn(InventarioController.class).listarInventario()).withSelfRel());
    }

    @Operation(summary = "Obtener inventario por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Inventario encontrado",
            content = @Content(mediaType = "application/hal+json", schema = @Schema(implementation = Inventario.class))),
        @ApiResponse(responseCode = "404", description = "Inventario no encontrado", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Sin permisos", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLEADO', 'GERENTE')")
    public ResponseEntity<EntityModel<Inventario>> obtenerInventarioPorId(
            @Parameter(description = "Identificador único del registro", example = "1", required = true)
            @PathVariable Long id) {
        Inventario inventario = inventarioService.findById(id);
        if (inventario == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(assembler.toModel(inventario));
    }

    @Operation(summary = "Crear nuevo registro de inventario")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Inventario creado exitosamente",
            content = @Content(mediaType = "application/hal+json", schema = @Schema(implementation = Inventario.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Sin permisos suficientes", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @PostMapping
    @PreAuthorize("hasRole('GERENTE')") 
    public ResponseEntity<EntityModel<Inventario>> guardarInventario(
            @Parameter(description = "Datos del inventario", required = true)
            @RequestBody Inventario inventario) {
        Inventario guardado = inventarioService.save(inventario);
        return ResponseEntity
                .created(linkTo(methodOn(InventarioController.class).obtenerInventarioPorId(guardado.getId())).toUri())
                .body(assembler.toModel(guardado));
    }

    @Operation(summary = "Actualizar registro de inventario")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Inventario actualizado correctamente",
            content = @Content(mediaType = "application/hal+json", schema = @Schema(implementation = Inventario.class))),
        @ApiResponse(responseCode = "404", description = "Inventario no encontrado", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Sin permisos", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('GERENTE')") 
    public ResponseEntity<EntityModel<Inventario>> actualizarInventario(
            @Parameter(description = "ID del inventario a modificar", example = "1", required = true)
            @PathVariable Long id, 
            @Parameter(description = "Nuevos datos", required = true)
            @RequestBody Inventario inventario) {
        if (inventarioService.findById(id) != null) {
            inventario.setId(id);
            return ResponseEntity.ok(assembler.toModel(inventarioService.save(inventario)));
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "Eliminar inventario")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Inventario eliminado correctamente"),
        @ApiResponse(responseCode = "404", description = "Inventario no encontrado", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Sin permisos suficientes", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('GERENTE')") // <-- CORREGIDO
    public ResponseEntity<Void> eliminarInventario(
            @Parameter(description = "ID del registro a eliminar", example = "1", required = true)
            @PathVariable Long id) {
        if (inventarioService.findById(id) != null) {
            inventarioService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}