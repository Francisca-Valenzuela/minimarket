package com.minimarket.controller;

import lombok.RequiredArgsConstructor;

import com.minimarket.assembler.ProductoModelAssembler;
import com.minimarket.dto.ProductoDTO;
import com.minimarket.entity.Categoria;
import com.minimarket.entity.Producto;
import com.minimarket.service.CategoriaService;
import com.minimarket.service.ProductoService;

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
@RequestMapping(value = "/api/productos", produces = { "application/hal+json", MediaType.APPLICATION_JSON_VALUE })
@Tag(name = "Productos", description = "Gestión del catálogo de productos del minimarket.")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    private final CategoriaService categoriaService;

    private final ProductoModelAssembler productoModelAssembler;

    @Operation(summary = "Listar todos los productos", description = "Retorna el catálogo completo de productos con sus respectivos enlaces HATEOAS.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente",
            content = @Content(mediaType = "application/hal+json", 
                array = @ArraySchema(schema = @Schema(implementation = Producto.class)))),
        @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Sin permisos suficientes", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLEADO', 'GERENTE')")
    public CollectionModel<EntityModel<Producto>> listarProductos() {
        List<EntityModel<Producto>> productos = productoService.findAll().stream()
                .map(productoModelAssembler::toModel)
                .collect(Collectors.toList());

        return CollectionModel.of(productos,
                linkTo(methodOn(ProductoController.class).listarProductos()).withSelfRel());
    }

    @Operation(summary = "Obtener producto por ID", description = "Devuelve los detalles exactos de un producto y su categoría.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Producto encontrado", 
            content = @Content(mediaType = "application/hal+json", schema = @Schema(implementation = Producto.class))),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Sin permisos", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLEADO', 'GERENTE')")
    public ResponseEntity<EntityModel<Producto>> obtenerProductoPorId(
            @Parameter(description = "Identificador único del producto", example = "3", required = true)
            @PathVariable Long id) {
        Producto producto = productoService.findById(id);
        if (producto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(productoModelAssembler.toModel(producto));
    }

    @Operation(summary = "Crear un nuevo producto", description = "Registra un nuevo producto en el catálogo. Requiere asociarlo a una categoría existente.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Producto creado correctamente", 
            content = @Content(mediaType = "application/hal+json", schema = @Schema(implementation = Producto.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos o categoría inexistente", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Sin permisos (Solo GERENTE)", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @PostMapping
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<?> guardarProducto(
            @Parameter(description = "Objeto con los datos del nuevo producto", required = true)
            @Valid @RequestBody ProductoDTO dto) {

        Categoria categoria = categoriaService.findById(dto.getCategoriaId());
        if (categoria == null) {
            return ResponseEntity.badRequest().body("La categoría especificada no existe");
        }

        Producto producto = new Producto();
        producto.setNombre(dto.getNombre());
        producto.setPrecio(dto.getPrecio());
        producto.setStock(dto.getStock());
        producto.setCategoria(categoria);

        Producto guardado = productoService.save(producto);
        EntityModel<Producto> model = productoModelAssembler.toModel(guardado);

        return ResponseEntity
                .created(linkTo(methodOn(ProductoController.class).obtenerProductoPorId(guardado.getId())).toUri())
                .body(model);
    }

    @Operation(summary = "Actualizar un producto existente", description = "Modifica los datos de un producto ya registrado en el catálogo.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Producto actualizado correctamente",
            content = @Content(mediaType = "application/hal+json", schema = @Schema(implementation = Producto.class))),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "400", description = "Datos inválidos o categoría inexistente", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Sin permisos suficientes", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLEADO', 'GERENTE')")
    public ResponseEntity<?> actualizarProducto(
            @Parameter(description = "ID del producto a actualizar", example = "3", required = true)
            @PathVariable Long id,
            @Parameter(description = "Nuevos datos del producto", required = true)
            @Valid @RequestBody ProductoDTO dto) {

        Producto existente = productoService.findById(id);
        if (existente == null) {
            return ResponseEntity.notFound().build();
        }

        Categoria categoria = categoriaService.findById(dto.getCategoriaId());
        if (categoria == null) {
            return ResponseEntity.badRequest().body("La categoría especificada no existe");
        }

        existente.setNombre(dto.getNombre());
        existente.setPrecio(dto.getPrecio());
        existente.setStock(dto.getStock());
        existente.setCategoria(categoria);

        Producto actualizado = productoService.save(existente);
        EntityModel<Producto> model = productoModelAssembler.toModel(actualizado);

        return ResponseEntity.ok(model);
    }

    @Operation(summary = "Eliminar un producto", description = "Elimina un producto del catálogo según su ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Producto eliminado correctamente"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Sin permisos suficientes", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLEADO', 'GERENTE')")
    public ResponseEntity<Void> eliminarProducto(
            @Parameter(description = "ID del producto a eliminar", example = "3", required = true)
            @PathVariable Long id) {
        Producto producto = productoService.findById(id);
        if (producto != null) {
            productoService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}