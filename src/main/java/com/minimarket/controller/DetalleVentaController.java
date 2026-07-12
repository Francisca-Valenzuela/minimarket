package com.minimarket.controller;

import com.minimarket.assembler.DetalleVentaModelAssembler;
import com.minimarket.entity.DetalleVenta;
import com.minimarket.service.DetalleVentaService;

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
@RequestMapping(value = "/api/detalle-ventas", produces = { "application/hal+json", MediaType.APPLICATION_JSON_VALUE })
@Tag(name = "Detalle de Ventas", description = "Gestión de los ítems o detalles específicos de cada venta.")
@SecurityRequirement(name = "bearerAuth")
public class DetalleVentaController {

    @Autowired
    private DetalleVentaService detalleVentaService;

    @Autowired
    private DetalleVentaModelAssembler assembler;

    @Operation(summary = "Listar detalles de ventas", description = "Retorna todos los detalles históricos registrados en el sistema.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente",
            content = @Content(mediaType = "application/hal+json",
                array = @ArraySchema(schema = @Schema(implementation = DetalleVenta.class)))),
        @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Sin permisos suficientes", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLEADO', 'GERENTE')")
    public CollectionModel<EntityModel<DetalleVenta>> listarDetalleVentas() {
        List<EntityModel<DetalleVenta>> detalles = detalleVentaService.findAll().stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(detalles, linkTo(methodOn(DetalleVentaController.class).listarDetalleVentas()).withSelfRel());
    }

    @Operation(summary = "Obtener detalle de venta por ID", description = "Retorna la información de una línea de venta específica.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Detalle encontrado",
            content = @Content(mediaType = "application/hal+json", schema = @Schema(implementation = DetalleVenta.class))),
        @ApiResponse(responseCode = "404", description = "Detalle no encontrado", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Sin permisos", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLEADO', 'GERENTE')")
    public ResponseEntity<EntityModel<DetalleVenta>> obtenerDetalleVentaPorId(
            @Parameter(description = "Identificador único del detalle de venta", example = "1", required = true)
            @PathVariable Long id) {
        DetalleVenta detalleVenta = detalleVentaService.findById(id);
        if (detalleVenta == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(assembler.toModel(detalleVenta));
    }

    @Operation(summary = "Guardar detalle de venta", description = "Crea un ítem de venta de manera aislada (uso interno).")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Detalle de venta creado exitosamente",
            content = @Content(mediaType = "application/hal+json", schema = @Schema(implementation = DetalleVenta.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Sin permisos suficientes", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('EMPLEADO', 'GERENTE')")
    public ResponseEntity<EntityModel<DetalleVenta>> guardarDetalleVenta(
            @Parameter(description = "Objeto con los datos del detalle", required = true)
            @RequestBody DetalleVenta detalleVenta) {
        DetalleVenta guardado = detalleVentaService.save(detalleVenta);
        return ResponseEntity
                .created(linkTo(methodOn(DetalleVentaController.class).obtenerDetalleVentaPorId(guardado.getId())).toUri())
                .body(assembler.toModel(guardado));
    }

    @Operation(summary = "Actualizar detalle de venta", description = "Modifica la cantidad u otros datos de un ítem ya registrado.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Detalle actualizado correctamente",
            content = @Content(mediaType = "application/hal+json", schema = @Schema(implementation = DetalleVenta.class))),
        @ApiResponse(responseCode = "404", description = "Detalle no encontrado", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Sin permisos", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLEADO', 'GERENTE')")
    public ResponseEntity<EntityModel<DetalleVenta>> actualizarDetalleVenta(
            @Parameter(description = "ID del detalle a modificar", example = "1", required = true)
            @PathVariable Long id, 
            @Parameter(description = "Nuevos datos del detalle", required = true)
            @RequestBody DetalleVenta detalleVenta) {
        if (detalleVentaService.findById(id) != null) {
            detalleVenta.setId(id);
            return ResponseEntity.ok(assembler.toModel(detalleVentaService.save(detalleVenta)));
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "Eliminar detalle de venta", description = "Borra un registro de detalle. Solo accesible para administradores.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Detalle de venta eliminado correctamente"), // Sin contenido
        @ApiResponse(responseCode = "404", description = "Detalle no encontrado", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Sin permisos suficientes", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<Void> eliminarDetalleVenta(
            @Parameter(description = "ID del registro a eliminar", example = "1", required = true)
            @PathVariable Long id) {
        if (detalleVentaService.findById(id) != null) {
            detalleVentaService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}