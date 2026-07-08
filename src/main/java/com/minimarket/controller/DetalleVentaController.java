package com.minimarket.controller;

import com.minimarket.assembler.DetalleVentaModelAssembler;
import com.minimarket.entity.DetalleVenta;
import com.minimarket.service.DetalleVentaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

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
@RequestMapping("/api/detalle-ventas")
@Tag(name = "Detalle de Ventas", description = "Gestión de los detalles de ventas del minimarket.")
@SecurityRequirement(name = "bearerAuth")
public class DetalleVentaController {

    @Autowired
    private DetalleVentaService detalleVentaService;

    @Autowired
    private DetalleVentaModelAssembler assembler;

    @Operation(summary = "Listar detalles de ventas")
    @ApiResponse(responseCode = "200", description = "Listado de detalles de venta con enlaces HATEOAS")
    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLEADO', 'GERENTE')")
    public CollectionModel<EntityModel<DetalleVenta>> listarDetalleVentas() {
        List<EntityModel<DetalleVenta>> detalles = detalleVentaService.findAll().stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(detalles, linkTo(methodOn(DetalleVentaController.class).listarDetalleVentas()).withSelfRel());
    }

    @Operation(summary = "Obtener detalle de venta por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Detalle encontrado"),
        @ApiResponse(responseCode = "404", description = "Detalle no encontrado", content = @Content)
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLEADO', 'GERENTE')")
    public ResponseEntity<EntityModel<DetalleVenta>> obtenerDetalleVentaPorId(@PathVariable Long id) {
        DetalleVenta detalleVenta = detalleVentaService.findById(id);
        if (detalleVenta == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(assembler.toModel(detalleVenta));
    }

    @Operation(summary = "Guardar detalle de venta")
    @ApiResponse(responseCode = "201", description = "Detalle de venta creado")
    @PostMapping
    @PreAuthorize("hasAnyRole('EMPLEADO', 'GERENTE')")
    public ResponseEntity<EntityModel<DetalleVenta>> guardarDetalleVenta(@RequestBody DetalleVenta detalleVenta) {
        DetalleVenta guardado = detalleVentaService.save(detalleVenta);
        return ResponseEntity
                .created(linkTo(methodOn(DetalleVentaController.class).obtenerDetalleVentaPorId(guardado.getId())).toUri())
                .body(assembler.toModel(guardado));
    }

    @Operation(summary = "Actualizar detalle de venta")
    @ApiResponse(responseCode = "200", description = "Detalle actualizado")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLEADO', 'GERENTE')")
    public ResponseEntity<EntityModel<DetalleVenta>> actualizarDetalleVenta(@PathVariable Long id, @RequestBody DetalleVenta detalleVenta) {
        if (detalleVentaService.findById(id) != null) {
            detalleVenta.setId(id);
            return ResponseEntity.ok(assembler.toModel(detalleVentaService.save(detalleVenta)));
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "Eliminar detalle de venta")
    @ApiResponse(responseCode = "204", description = "Detalle de venta eliminado", content = @Content)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<Void> eliminarDetalleVenta(@PathVariable Long id) {
        if (detalleVentaService.findById(id) != null) {
            detalleVentaService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}