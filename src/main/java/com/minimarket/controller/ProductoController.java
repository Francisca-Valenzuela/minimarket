package com.minimarket.controller;


import com.minimarket.dto.ProductoDTO;
import com.minimarket.entity.Categoria;
import com.minimarket.entity.Producto;
import com.minimarket.service.CategoriaService;
import com.minimarket.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@Tag(name = "Productos", description = "Gestión del catálogo de productos del minimarket, incluyendo creación y listado de productos.")
@SecurityRequirement(name = "bearerAuth")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private CategoriaService categoriaService;

    @Operation(
        summary = "Listar todos los productos",
        description = "Retorna el listado completo de productos disponibles en el catálogo."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Producto.class))),
        @ApiResponse(responseCode = "401", description = "No autenticado"),
        @ApiResponse(responseCode = "403", description = "Sin permisos suficientes (requiere rol EMPLEADO o GERENTE)")
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLEADO', 'GERENTE')")
    public List<Producto> listarProductos() {
        return productoService.findAll();
    }

    @Operation(
        summary = "Crear un nuevo producto",
        description = "Registra un producto nuevo asociado a una categoría existente. " +
                "Valida el nombre (sin caracteres peligrosos), precio y stock no negativos."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Producto creado correctamente",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Producto.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos o categoría inexistente",
            content = @Content(mediaType = "application/json",
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = "\"La categoría especificada no existe\""))),
        @ApiResponse(responseCode = "401", description = "No autenticado"),
        @ApiResponse(responseCode = "403", description = "Sin permisos suficientes")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('EMPLEADO', 'GERENTE')")
    public ResponseEntity<?> guardarProducto(
            @Parameter(description = "Datos del producto a crear", required = true)
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

        return ResponseEntity.ok(productoService.save(producto));
    }
}