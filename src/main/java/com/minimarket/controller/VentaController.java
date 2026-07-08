package com.minimarket.controller;

import com.minimarket.assembler.VentaModelAssembler;
import com.minimarket.entity.Venta;
import com.minimarket.service.VentaService;

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
@RequestMapping("/api/ventas")
@Tag(name = "Ventas", description = "Gestión de las ventas del minimarket.")
@SecurityRequirement(name = "bearerAuth")
public class VentaController {

    @Autowired
    private VentaService ventaService;

    @Autowired
    private VentaModelAssembler assembler;

    @Operation(summary = "Listar todas las ventas")
    @ApiResponse(responseCode = "200", description = "Listado de ventas con enlaces HATEOAS")
    @GetMapping
    @PreAuthorize("hasAnyRole('GERENTE', 'EMPLEADO')")
    public CollectionModel<EntityModel<Venta>> listarVentas() {
        List<EntityModel<Venta>> ventas = ventaService.findAll().stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(ventas, linkTo(methodOn(VentaController.class).listarVentas()).withSelfRel());
    }

    @Operation(summary = "Obtener venta por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Venta encontrada"),
        @ApiResponse(responseCode = "404", description = "Venta no encontrada", content = @Content)
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('GERENTE', 'EMPLEADO')")
    public ResponseEntity<EntityModel<Venta>> obtenerVentaPorId(@PathVariable Long id) {
        Venta venta = ventaService.findById(id);
        if (venta == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(assembler.toModel(venta));
    }

    @Operation(summary = "Registrar una nueva venta")
    @ApiResponse(responseCode = "201", description = "Venta generada correctamente")
    @PostMapping
    @PreAuthorize("hasAnyRole('GERENTE', 'EMPLEADO')") 
    public ResponseEntity<EntityModel<Venta>> guardarVenta(@RequestBody Venta venta) {
        Venta guardada = ventaService.save(venta);
        return ResponseEntity
                .created(linkTo(methodOn(VentaController.class).obtenerVentaPorId(guardada.getId())).toUri())
                .body(assembler.toModel(guardada));
    }
}