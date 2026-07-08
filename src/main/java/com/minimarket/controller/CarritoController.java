package com.minimarket.controller;

import com.minimarket.assembler.CarritoModelAssembler;
import com.minimarket.entity.Carrito;
import com.minimarket.service.CarritoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
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
@RequestMapping("/api/carrito")
@Tag(name = "Carrito", description = "Gestión del carrito de compras de los clientes")
@SecurityRequirement(name = "bearerAuth")
public class CarritoController {

    @Autowired
    private CarritoService carritoService;

    @Autowired
    private CarritoModelAssembler carritoModelAssembler;

    @Operation(
        summary = "Listar todos los carritos",
        description = "Retorna todos los registros de carrito existentes en el sistema, " +
                "con enlaces HATEOAS a cada ítem y al producto asociado."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Carrito.class))),
        @ApiResponse(responseCode = "401", description = "No autenticado"),
        @ApiResponse(responseCode = "403", description = "Sin permisos suficientes")
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENTE', 'EMPLEADO', 'GERENTE')")
    public CollectionModel<EntityModel<Carrito>> listarCarrito() {
        List<EntityModel<Carrito>> carritos = carritoService.findAll().stream()
                .map(carritoModelAssembler::toModel)
                .collect(Collectors.toList());

        return CollectionModel.of(carritos,
                linkTo(methodOn(CarritoController.class).listarCarrito()).withSelfRel());
    }

    @Operation(
        summary = "Obtener un carrito por ID",
        description = "Busca un registro de carrito específico según su identificador y " +
                "retorna sus enlaces HATEOAS (self, colección y producto asociado)."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Carrito encontrado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Carrito.class))),
        @ApiResponse(responseCode = "404", description = "Carrito no encontrado", content = @Content),
        @ApiResponse(responseCode = "401", description = "No autenticado"),
        @ApiResponse(responseCode = "403", description = "Sin permisos suficientes")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'EMPLEADO', 'GERENTE')")
    public ResponseEntity<EntityModel<Carrito>> obtenerCarritoPorId(
            @Parameter(description = "ID del carrito", example = "1", required = true)
            @PathVariable Long id) {
        Carrito carrito = carritoService.findById(id);
        if (carrito == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(carritoModelAssembler.toModel(carrito));
    }

    @Operation(
        summary = "Agregar un producto al carrito",
        description = "Crea un nuevo registro de carrito asociando un usuario, un producto y una cantidad."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Producto agregado correctamente",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Carrito.class),
                examples = @ExampleObject(
                    name = "Ejemplo de carrito",
                    value = "{\"usuario\": {\"id\": 1}, \"producto\": {\"id\": 3}, \"cantidad\": 2}"
                ))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "401", description = "No autenticado"),
        @ApiResponse(responseCode = "403", description = "Sin permisos suficientes")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENTE', 'EMPLEADO', 'GERENTE')")
    public ResponseEntity<EntityModel<Carrito>> agregarProductoAlCarrito(
            @Parameter(description = "Datos del carrito a crear", required = true)
            @RequestBody Carrito carrito) {
        Carrito guardado = carritoService.save(carrito);
        EntityModel<Carrito> model = carritoModelAssembler.toModel(guardado);
        return ResponseEntity
                .created(linkTo(methodOn(CarritoController.class).obtenerCarritoPorId(guardado.getId())).toUri())
                .body(model);
    }

    @Operation(
        summary = "Actualizar un carrito existente",
        description = "Modifica la cantidad u otros datos de un registro de carrito ya existente."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Carrito actualizado correctamente",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Carrito.class))),
        @ApiResponse(responseCode = "404", description = "Carrito no encontrado", content = @Content),
        @ApiResponse(responseCode = "401", description = "No autenticado"),
        @ApiResponse(responseCode = "403", description = "Sin permisos suficientes")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'EMPLEADO', 'GERENTE')")
    public ResponseEntity<EntityModel<Carrito>> actualizarCarrito(
            @Parameter(description = "ID del carrito a actualizar", example = "1", required = true)
            @PathVariable Long id,
            @Parameter(description = "Nuevos datos del carrito", required = true)
            @RequestBody Carrito carrito) {
        Carrito existente = carritoService.findById(id);
        if (existente != null) {
            carrito.setId(id);
            Carrito actualizado = carritoService.save(carrito);
            return ResponseEntity.ok(carritoModelAssembler.toModel(actualizado));
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(
        summary = "Eliminar un producto del carrito",
        description = "Elimina un registro de carrito según su ID. Requiere rol EMPLEADO o GERENTE."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Carrito eliminado correctamente", content = @Content),
        @ApiResponse(responseCode = "404", description = "Carrito no encontrado", content = @Content),
        @ApiResponse(responseCode = "401", description = "No autenticado"),
        @ApiResponse(responseCode = "403", description = "Sin permisos suficientes (requiere rol EMPLEADO o GERENTE)")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLEADO', 'GERENTE')")
    public ResponseEntity<Void> eliminarProductoDelCarrito(
            @Parameter(description = "ID del carrito a eliminar", example = "1", required = true)
            @PathVariable Long id) {
        Carrito carrito = carritoService.findById(id);
        if (carrito != null) {
            carritoService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
