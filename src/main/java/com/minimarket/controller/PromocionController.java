package com.minimarket.controller;

import com.minimarket.dto.*;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Promocion;
import com.minimarket.entity.Sucursal;
import com.minimarket.entity.TipoDescuento;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.PromocionRepository;
import com.minimarket.repository.SucursalRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/promociones")
@Tag(name = "Promociones", description = "Gestión de descuentos por producto, globales o por sucursal")
public class PromocionController {

    private final PromocionRepository promocionRepository;
    private final ProductoRepository productoRepository;
    private final SucursalRepository sucursalRepository;

    public PromocionController(PromocionRepository promocionRepository,
                                ProductoRepository productoRepository,
                                SucursalRepository sucursalRepository) {
        this.promocionRepository = promocionRepository;
        this.productoRepository = productoRepository;
        this.sucursalRepository = sucursalRepository;
    }

    @GetMapping
    @Operation(summary = "Listar todas las promociones")
    public ResponseEntity<List<PromocionResponseDTO>> listar() {
        List<PromocionResponseDTO> dtos = promocionRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener promoción por ID")
    public ResponseEntity<PromocionResponseDTO> obtener(@PathVariable Long id) {
        Promocion promo = promocionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promoción no encontrada"));
        return ResponseEntity.ok(mapToDTO(promo));
    }

    @PostMapping
    @PreAuthorize("hasRole('GERENTE')")
    @Operation(summary = "Crear una nueva promoción (global o por sucursal)")
    public ResponseEntity<PromocionResponseDTO> crear(@RequestBody PromocionRequestDTO request) {
        Promocion promo = new Promocion();
        aplicarDatosDelRequest(promo, request);
        Promocion guardada = promocionRepository.save(promo);
        return ResponseEntity.ok(mapToDTO(guardada));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('GERENTE')")
    @Operation(summary = "Actualizar una promoción existente")
    public ResponseEntity<PromocionResponseDTO> actualizar(@PathVariable Long id,
                                                             @RequestBody PromocionRequestDTO request) {
        Promocion promo = promocionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promoción no encontrada"));
        aplicarDatosDelRequest(promo, request);
        Promocion actualizada = promocionRepository.save(promo);
        return ResponseEntity.ok(mapToDTO(actualizada));
    }

    @PatchMapping("/{id}/desactivar")
    @PreAuthorize("hasRole('GERENTE')")
    @Operation(summary = "Desactivar una promoción sin eliminarla")
    public ResponseEntity<PromocionResponseDTO> desactivar(@PathVariable Long id) {
        Promocion promo = promocionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promoción no encontrada"));
        promo.setActiva(false);
        Promocion actualizada = promocionRepository.save(promo);
        return ResponseEntity.ok(mapToDTO(actualizada));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('GERENTE')")
    @Operation(summary = "Eliminar una promoción permanentemente")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        promocionRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // Helpers privados
    // ============================================================

    private void aplicarDatosDelRequest(Promocion promo, PromocionRequestDTO request) {
        Producto producto = productoRepository.findById(request.getProductoId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        promo.setProducto(producto);

        if (request.getSucursalId() != null) {
            Sucursal sucursal = sucursalRepository.findById(request.getSucursalId())
                    .orElseThrow(() -> new RuntimeException("Sucursal no encontrada"));
            promo.setSucursal(sucursal);
        } else {
            promo.setSucursal(null); // promoción global
        }

        promo.setTipoDescuento(TipoDescuento.valueOf(request.getTipoDescuento()));
        promo.setValor(request.getValor());
        promo.setFechaInicio(request.getFechaInicio());
        promo.setFechaFin(request.getFechaFin());
    }

    private PromocionResponseDTO mapToDTO(Promocion promo) {
        ProductoResumenDTO productoDTO = new ProductoResumenDTO(
                promo.getProducto().getId(),
                promo.getProducto().getNombre(),
                promo.getProducto().getPrecio()
        );

        SucursalResumenDTO sucursalDTO = null;
        if (promo.getSucursal() != null) {
            sucursalDTO = new SucursalResumenDTO(
                    promo.getSucursal().getId(),
                    promo.getSucursal().getNombre(),
                    promo.getSucursal().getDireccion()
            );
        }

        return new PromocionResponseDTO(
                promo.getId(),
                productoDTO,
                sucursalDTO,
                promo.getTipoDescuento().name(),
                promo.getValor(),
                promo.getFechaInicio(),
                promo.getFechaFin(),
                promo.getActiva()
        );
    }
}
