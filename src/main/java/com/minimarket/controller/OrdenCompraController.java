package com.minimarket.controller;

import com.minimarket.dto.OrdenCompraResponseDTO;
import com.minimarket.dto.ProductoResumenDTO;
import com.minimarket.dto.ProveedorResumenDTO;
import com.minimarket.dto.SucursalResumenDTO;
import com.minimarket.entity.EstadoOrdenCompra;
import com.minimarket.entity.OrdenCompra;
import com.minimarket.service.OrdenCompraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ordenes-compra")
@Tag(name = "Órdenes de Compra", description = "Órdenes de reposición a proveedores")
@PreAuthorize("hasAnyRole('GERENTE','JEFE_TURNO')")
public class OrdenCompraController {

    private final OrdenCompraService ordenCompraService;

    public OrdenCompraController(OrdenCompraService ordenCompraService) {
        this.ordenCompraService = ordenCompraService;
    }

    @GetMapping
    @Operation(summary = "Listar todas las órdenes de compra")
    public ResponseEntity<List<OrdenCompraResponseDTO>> listar() {
        List<OrdenCompraResponseDTO> dtos = ordenCompraService.listarTodas()
                .stream()
                .map(this::mapToDTO)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/estado/{estado}")
    @Operation(summary = "Listar órdenes por estado (PENDIENTE, ENVIADA, RECIBIDA, CANCELADA)")
    public ResponseEntity<List<OrdenCompraResponseDTO>> porEstado(@PathVariable EstadoOrdenCompra estado) {
        List<OrdenCompraResponseDTO> dtos = ordenCompraService.listarPorEstado(estado)
                .stream()
                .map(this::mapToDTO)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener orden por ID")
    public ResponseEntity<OrdenCompraResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(mapToDTO(ordenCompraService.obtenerPorId(id)));
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Cambiar estado. RECIBIDA ingresa automáticamente el stock a la sucursal")
    public ResponseEntity<OrdenCompraResponseDTO> cambiarEstado(@PathVariable Long id,
                                                     @RequestParam EstadoOrdenCompra estado) {
        return ResponseEntity.ok(mapToDTO(ordenCompraService.cambiarEstado(id, estado)));
    }

    // ============================================================
    // Mapeo privado: entidad JPA -> DTO (rompe el ciclo de recursión)
    // ============================================================

    private OrdenCompraResponseDTO mapToDTO(OrdenCompra orden) {
        ProveedorResumenDTO proveedorDTO = new ProveedorResumenDTO(
                orden.getProveedor().getId(),
                orden.getProveedor().getNombre(),
                orden.getProveedor().getRut(),
                orden.getProveedor().getEmail(),
                orden.getProveedor().getTelefono()
        );

        ProductoResumenDTO productoDTO = new ProductoResumenDTO(
                orden.getProducto().getId(),
                orden.getProducto().getNombre(),
                orden.getProducto().getPrecio()
        );

        SucursalResumenDTO sucursalDTO = new SucursalResumenDTO(
                orden.getSucursal().getId(),
                orden.getSucursal().getNombre(),
                orden.getSucursal().getDireccion()
        );

        return new OrdenCompraResponseDTO(
                orden.getId(),
                proveedorDTO,
                productoDTO,
                sucursalDTO,
                orden.getCantidadSolicitada(),
                orden.getFechaCreacion(),
                orden.getEstado().name(),
                orden.getAutomatica()
        );
    }
}
