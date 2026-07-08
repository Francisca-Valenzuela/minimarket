package com.minimarket.controller;



import com.minimarket.assembler.InventarioModelAssembler;
import com.minimarket.entity.Inventario;
import com.minimarket.service.InventarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

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

@Slf4j
@RestController
@RequestMapping("/api/inventario")
@Tag(name = "Inventario", description = "Gestión del inventario del minimarket, incluyendo movimientos de stock.")
@SecurityRequirement(name = "bearerAuth")
public class InventarioController {

    @Autowired
    private InventarioService inventarioService;

    @Autowired
    private InventarioModelAssembler inventarioModelAssembler;

    @Operation(
        summary = "Listar movimientos de inventario",
        description = "Retorna todos los movimientos de stock registrados (entradas y salidas), " +
                "con enlaces HATEOAS a cada movimiento y al producto afectado."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Inventario.class))),
        @ApiResponse(responseCode = "401", description = "No autenticado"),
        @ApiResponse(responseCode = "403", description = "Sin permisos suficientes (requiere rol GERENTE o EMPLEADO)")
    })
    // LECTURAS: Permitidas para Gerente y Empleado (necesitan ver stock)
    @GetMapping
    @PreAuthorize("hasAnyRole('GERENTE', 'EMPLEADO')")
    public CollectionModel<EntityModel<Inventario>> listarMovimientosDeInventario() {
        List<EntityModel<Inventario>> movimientos = inventarioService.findAll().stream()
                .map(inventarioModelAssembler::toModel)
                .collect(Collectors.toList());

        return CollectionModel.of(movimientos,
                linkTo(methodOn(InventarioController.class).listarMovimientosDeInventario()).withSelfRel());
    }

    @Operation(
        summary = "Obtener un movimiento de inventario por ID",
        description = "Busca un movimiento de stock específico según su identificador, retornando " +
                "sus enlaces HATEOAS (self, colección de movimientos y producto asociado)."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Movimiento encontrado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Inventario.class))),
        @ApiResponse(responseCode = "404", description = "Movimiento no encontrado", content = @Content),
        @ApiResponse(responseCode = "401", description = "No autenticado"),
        @ApiResponse(responseCode = "403", description = "Sin permisos suficientes")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('GERENTE', 'EMPLEADO')")
    public ResponseEntity<EntityModel<Inventario>> obtenerMovimientoPorId(
            @Parameter(description = "ID del movimiento de inventario", example = "1", required = true)
            @PathVariable Long id) {
        Inventario inventario = inventarioService.findById(id);
        if (inventario == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(inventarioModelAssembler.toModel(inventario));
    }

    @Operation(
        summary = "Registrar un movimiento de inventario",
        description = "Crea un nuevo registro de entrada o salida de stock para un producto. " +
                "Restringido estrictamente al rol GERENTE."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Movimiento registrado correctamente",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Inventario.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "401", description = "No autenticado"),
        @ApiResponse(responseCode = "403", description = "Sin permisos suficientes (requiere rol GERENTE)")
    })
    // ESCRITURAS: Restringidas estrictamente al Gerente
    @PostMapping
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<EntityModel<Inventario>> registrarMovimiento(
            @Parameter(description = "Datos del movimiento de inventario a registrar", required = true)
            @RequestBody Inventario inventario) {
        // En una app real, el ID del producto no debería ser null aquí. Extraemos el ID con seguridad.
        Long productoId = (inventario.getProducto() != null) ? inventario.getProducto().getId() : null;
        log.info("Auditoría - Registrando nuevo movimiento de inventario: Producto ID [{}], Tipo [{}], Cantidad [{}]",
                 productoId, inventario.getTipoMovimiento(), inventario.getCantidad());

        Inventario guardado = inventarioService.save(inventario);
        EntityModel<Inventario> model = inventarioModelAssembler.toModel(guardado);
        return ResponseEntity
                .created(linkTo(methodOn(InventarioController.class).obtenerMovimientoPorId(guardado.getId())).toUri())
                .body(model);
    }

    @Operation(
        summary = "Actualizar un movimiento de inventario",
        description = "Modifica los datos de un movimiento de stock existente. Restringido al rol GERENTE."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Movimiento actualizado correctamente",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Inventario.class))),
        @ApiResponse(responseCode = "404", description = "Movimiento no encontrado", content = @Content),
        @ApiResponse(responseCode = "401", description = "No autenticado"),
        @ApiResponse(responseCode = "403", description = "Sin permisos suficientes (requiere rol GERENTE)")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<EntityModel<Inventario>> actualizarMovimiento(
            @Parameter(description = "ID del movimiento a actualizar", example = "1", required = true)
            @PathVariable Long id,
            @Parameter(description = "Nuevos datos del movimiento", required = true)
            @RequestBody Inventario inventario) {
        log.info("Auditoría - Intento de actualización de movimiento de inventario ID: {}", id);

        Inventario existente = inventarioService.findById(id);
        if (existente != null) {
            inventario.setId(id);
            Inventario actualizado = inventarioService.save(inventario);
            log.info("Auditoría - Movimiento de inventario ID: {} actualizado exitosamente", id);
            return ResponseEntity.ok(inventarioModelAssembler.toModel(actualizado));
        }

        log.warn("Auditoría - Fallo al actualizar: Movimiento de inventario ID {} no encontrado", id);
        return ResponseEntity.notFound().build();
    }

    @Operation(
        summary = "Eliminar un movimiento de inventario",
        description = "Elimina un movimiento de stock según su ID. Restringido al rol GERENTE."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Movimiento eliminado correctamente", content = @Content),
        @ApiResponse(responseCode = "404", description = "Movimiento no encontrado", content = @Content),
        @ApiResponse(responseCode = "401", description = "No autenticado"),
        @ApiResponse(responseCode = "403", description = "Sin permisos suficientes (requiere rol GERENTE)")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<Void> eliminarMovimiento(
            @Parameter(description = "ID del movimiento a eliminar", example = "1", required = true)
            @PathVariable Long id) {
        log.warn("Auditoría Crítica - Petición para ELIMINAR movimiento de inventario ID: {}", id);

        Inventario inventario = inventarioService.findById(id);
        if (inventario != null) {
            inventarioService.deleteById(id);
            log.info("Auditoría Crítica - Movimiento de inventario ID: {} eliminado exitosamente", id);
            return ResponseEntity.noContent().build();
        }

        log.warn("Auditoría - Fallo al eliminar: Movimiento de inventario ID {} no existe", id);
        return ResponseEntity.notFound().build();
    }
}
