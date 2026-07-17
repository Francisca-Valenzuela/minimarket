package com.minimarket.controller;

import com.minimarket.dto.DisponibilidadDTO;
import com.minimarket.dto.SucursalResponseDTO;
import com.minimarket.dto.StockSucursalResumenDTO;
import com.minimarket.entity.StockSucursal;
import com.minimarket.entity.Sucursal;
import com.minimarket.service.SucursalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/sucursales")
@Tag(name = "Sucursales", description = "Gestión de sucursales y stock distribuido")
public class SucursalController {

    private final SucursalService sucursalService;

    public SucursalController(SucursalService sucursalService) {
        this.sucursalService = sucursalService;
    }

    @GetMapping
    @Operation(summary = "Listar todas las sucursales activas")
    public ResponseEntity<List<SucursalResponseDTO>> listar() {
        List<SucursalResponseDTO> dtos = sucursalService.listarActivas()
                .stream()
                .map(this::mapToDTO)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener sucursal por ID")
    public ResponseEntity<SucursalResponseDTO> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(mapToDTO(sucursalService.obtenerPorId(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('GERENTE')")
    @Operation(summary = "Crear sucursal (solo Gerente)")
    public ResponseEntity<SucursalResponseDTO> crear(@Valid @RequestBody Sucursal sucursal) {
        Sucursal creada = sucursalService.crear(sucursal);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToDTO(creada));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('GERENTE')")
    @Operation(summary = "Actualizar sucursal (solo Gerente)")
    public ResponseEntity<SucursalResponseDTO> actualizar(@PathVariable Long id,
                                               @Valid @RequestBody Sucursal sucursal) {
        return ResponseEntity.ok(mapToDTO(sucursalService.actualizar(id, sucursal)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('GERENTE')")
    @Operation(summary = "Desactivar sucursal (baja lógica, solo Gerente)")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        sucursalService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/stock")
    @PreAuthorize("hasAnyRole('GERENTE','JEFE_TURNO','REPONEDOR')")
    @Operation(summary = "Ver stock de una sucursal")
    public ResponseEntity<List<StockSucursalResumenDTO>> stockPorSucursal(@PathVariable Long id) {
        List<StockSucursalResumenDTO> dtos = sucursalService.listarStockPorSucursal(id)
                .stream()
                .map(this::mapToStockDTO)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/{sucursalId}/stock/{productoId}")
    @PreAuthorize("hasAnyRole('GERENTE','JEFE_TURNO')")
    @Operation(summary = "Asignar/definir stock y stock mínimo de un producto en una sucursal")
    public ResponseEntity<StockSucursalResumenDTO> asignarStock(@PathVariable Long sucursalId,
                                                      @PathVariable Long productoId,
                                                      @RequestParam Integer cantidad,
                                                      @RequestParam Integer stockMinimo) {
        StockSucursal creado = sucursalService.asignarStock(sucursalId, productoId, cantidad, stockMinimo);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToStockDTO(creado));
    }

    @PatchMapping("/{sucursalId}/stock/{productoId}/ajustar")
    @PreAuthorize("hasAnyRole('GERENTE','JEFE_TURNO','REPONEDOR')")
    @Operation(summary = "Ajustar stock (+entrada / -salida). Gatilla reposición automática si baja del mínimo")
    public ResponseEntity<StockSucursalResumenDTO> ajustarStock(@PathVariable Long sucursalId,
                                                      @PathVariable Long productoId,
                                                      @RequestParam Integer delta) {
        StockSucursal ajustado = sucursalService.ajustarStock(sucursalId, productoId, delta);
        return ResponseEntity.ok(mapToStockDTO(ajustado));
    }

    @GetMapping("/disponibilidad/{productoId}")
    @Operation(summary = "Consultar disponibilidad de un producto en todas las sucursales (público)")
    public ResponseEntity<List<DisponibilidadDTO>> disponibilidad(@PathVariable Long productoId) {
        return ResponseEntity.ok(sucursalService.consultarDisponibilidad(productoId));
    }

    // ============================================================
    // Métodos privados de mapeo: entidad JPA -> DTO (rompen el ciclo)
    // ============================================================

    private SucursalResponseDTO mapToDTO(Sucursal sucursal) {
        List<StockSucursalResumenDTO> stocksDTO = (sucursal.getStocks() == null)
                ? Collections.emptyList()
                : sucursal.getStocks().stream().map(this::mapToStockDTO).toList();

        return new SucursalResponseDTO(
                sucursal.getId(),
                sucursal.getNombre(),
                sucursal.getDireccion(),
                sucursal.getComuna(),
                sucursal.getTelefono(),
                sucursal.getActiva(),
                stocksDTO
        );
    }

    private StockSucursalResumenDTO mapToStockDTO(StockSucursal stock) {
        return new StockSucursalResumenDTO(
                stock.getId(),
                stock.getProducto().getId(),
                stock.getProducto().getNombre(),
                stock.getCantidad(),
                stock.getStockMinimo()
        );
    }
}
