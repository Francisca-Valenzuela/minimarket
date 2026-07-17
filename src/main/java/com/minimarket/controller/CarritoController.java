package com.minimarket.controller;

import lombok.RequiredArgsConstructor;

import com.minimarket.assembler.CarritoModelAssembler;
import com.minimarket.entity.Carrito;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Usuario;
import com.minimarket.service.CarritoService;
import com.minimarket.service.ProductoService;
import com.minimarket.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
@RequestMapping(value = "/api/carrito", produces = { "application/hal+json", MediaType.APPLICATION_JSON_VALUE })
@Tag(name = "Carrito", description = "Gestión del carrito de compras de los clientes")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class CarritoController {

    private final CarritoService carritoService;

    private final CarritoModelAssembler carritoModelAssembler;

    private final ProductoService productoService;

    private final UsuarioService usuarioService;

    @Operation(
        summary = "Listar todos los carritos",
        description = "Retorna todos los registros de carrito existentes en el sistema, con enlaces HATEOAS a cada ítem y al producto asociado."
    )
    @ApiResponses(value = {        
        @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente",
            content = @Content(mediaType = "application/hal+json",
                array = @ArraySchema(schema = @Schema(implementation = Carrito.class)))),
        @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Sin permisos suficientes", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
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
        description = "Busca un registro de carrito específico según su identificador y retorna sus enlaces HATEOAS (self, colección y producto asociado)."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Carrito encontrado",
            content = @Content(mediaType = "application/hal+json",
                schema = @Schema(implementation = Carrito.class))),
        @ApiResponse(responseCode = "404", description = "Carrito no encontrado", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Sin permisos suficientes", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'EMPLEADO', 'GERENTE')")
    public ResponseEntity<EntityModel<Carrito>> obtenerCarritoPorId(
            @Parameter(description = "Identificador único del registro en el carrito", example = "1", required = true)
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
        @ApiResponse(responseCode = "201", description = "Producto agregado correctamente",
            content = @Content(mediaType = "application/hal+json",
                schema = @Schema(implementation = Carrito.class),
                examples = @ExampleObject(
                    name = "Ejemplo de payload para nuevo carrito",
                    value = "{\"usuario\": {\"id\": 1}, \"producto\": {\"id\": 3}, \"cantidad\": 2}"
                ))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos, usuario o producto inexistente", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Sin permisos suficientes", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENTE', 'EMPLEADO', 'GERENTE')")
    public ResponseEntity<?> agregarProductoAlCarrito(
            @Parameter(description = "Objeto con los datos del carrito a crear", required = true)
            @Valid @RequestBody Carrito carrito) {

        Usuario usuario = usuarioService.findById(carrito.getUsuario().getId()).orElse(null);
        Producto producto = productoService.findById(carrito.getProducto().getId());

        if (usuario == null || producto == null) {
            return ResponseEntity.badRequest().body("Usuario o producto inexistente");
        }

        carrito.setUsuario(usuario);
        carrito.setProducto(producto);

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
            content = @Content(mediaType = "application/hal+json",
                schema = @Schema(implementation = Carrito.class))),
        @ApiResponse(responseCode = "404", description = "Carrito no encontrado", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "400", description = "Datos inválidos, usuario o producto inexistente", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Sin permisos suficientes", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'EMPLEADO', 'GERENTE')")
    public ResponseEntity<?> actualizarCarrito(
            @Parameter(description = "Identificador único del registro a actualizar", example = "1", required = true)
            @PathVariable Long id,
            @Parameter(description = "Nuevos datos de actualización", required = true)
            @Valid @RequestBody Carrito carrito) {
        Carrito existente = carritoService.findById(id);
        if (existente == null) {
            return ResponseEntity.notFound().build();
        }

        Usuario usuario = usuarioService.findById(carrito.getUsuario().getId()).orElse(null);
        Producto producto = productoService.findById(carrito.getProducto().getId());

        if (usuario == null || producto == null) {
            return ResponseEntity.badRequest().body("Usuario o producto inexistente");
        }

        carrito.setId(id);
        carrito.setUsuario(usuario);
        carrito.setProducto(producto);

        Carrito actualizado = carritoService.save(carrito);
        return ResponseEntity.ok(carritoModelAssembler.toModel(actualizado));
    }

    @Operation(
        summary = "Eliminar un producto del carrito",
        description = "Elimina un registro de carrito según su ID. Requiere rol EMPLEADO o GERENTE."
    )
    @ApiResponses(value = {
        
        @ApiResponse(responseCode = "204", description = "Carrito eliminado correctamente"),
        @ApiResponse(responseCode = "404", description = "Carrito no encontrado", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Sin permisos suficientes", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLEADO', 'GERENTE')")
    public ResponseEntity<Void> eliminarProductoDelCarrito(
            @Parameter(description = "Identificador del registro a eliminar", example = "1", required = true)
            @PathVariable Long id) {
        Carrito carrito = carritoService.findById(id);
        if (carrito != null) {
            carritoService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}