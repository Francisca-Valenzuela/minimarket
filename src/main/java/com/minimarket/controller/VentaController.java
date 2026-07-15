package com.minimarket.controller;

import lombok.RequiredArgsConstructor;

import com.minimarket.assembler.VentaModelAssembler;
import com.minimarket.dto.VentaResponseDTO;
import com.minimarket.entity.Venta;
import com.minimarket.service.VentaService;

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
@RequestMapping(value = "/api/ventas", produces = { "application/hal+json", MediaType.APPLICATION_JSON_VALUE })
@Tag(name = "Ventas", description = "Gestión del registro e historial de ventas del minimarket.")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class VentaController {

    private final VentaService ventaService;

    private final VentaModelAssembler assembler;

    @Operation(summary = "Listar todas las ventas", description = "Retorna el historial de ventas utilizando un DTO limpio y enlaces HATEOAS.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente",
            content = @Content(mediaType = "application/hal+json",
                array = @ArraySchema(schema = @Schema(implementation = VentaResponseDTO.class)))),
        @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Sin permisos suficientes", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('GERENTE', 'EMPLEADO')")
    public CollectionModel<EntityModel<VentaResponseDTO>> listarVentas() {
        List<EntityModel<VentaResponseDTO>> ventas = ventaService.findAll().stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(ventas, linkTo(methodOn(VentaController.class).listarVentas()).withSelfRel());
    }

    @Operation(summary = "Obtener venta por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Venta encontrada", 
            content = @Content(mediaType = "application/hal+json", schema = @Schema(implementation = VentaResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Venta no encontrada", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Sin permisos suficientes", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('GERENTE', 'EMPLEADO')")
    public ResponseEntity<EntityModel<VentaResponseDTO>> obtenerVentaPorId(
            @Parameter(description = "Identificador único de la venta", example = "1", required = true)
            @PathVariable Long id) {
        Venta venta = ventaService.findById(id);
        if (venta == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(assembler.toModel(venta));
    }

    @Operation(summary = "Registrar una nueva venta")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Venta registrada correctamente", 
            content = @Content(mediaType = "application/hal+json", schema = @Schema(implementation = VentaResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Sin permisos suficientes", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('GERENTE', 'EMPLEADO')") 
    public ResponseEntity<EntityModel<VentaResponseDTO>> guardarVenta(
            @Parameter(description = "Payload con los datos de la venta", required = true)
            @Valid @RequestBody Venta venta) {
        Venta guardada = ventaService.save(venta);
        return ResponseEntity
                .created(linkTo(methodOn(VentaController.class).obtenerVentaPorId(guardada.getId())).toUri())
                .body(assembler.toModel(guardada));
    }
}