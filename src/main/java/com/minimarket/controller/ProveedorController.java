package com.minimarket.controller;

import com.minimarket.entity.Proveedor;
import com.minimarket.repository.ProveedorRepository;
import com.minimarket.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/proveedores")
@Tag(name = "Proveedores", description = "Gestión de proveedores")
@PreAuthorize("hasAnyRole('GERENTE','JEFE_TURNO')")
public class ProveedorController {

    private final ProveedorRepository proveedorRepository;

    public ProveedorController(ProveedorRepository proveedorRepository) {
        this.proveedorRepository = proveedorRepository;
    }

    @GetMapping
    @Operation(summary = "Listar proveedores activos")
    public ResponseEntity<List<Proveedor>> listar() {
        return ResponseEntity.ok(proveedorRepository.findByActivoTrue());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener proveedor por ID")
    public ResponseEntity<Proveedor> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(proveedorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado con id: " + id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('GERENTE')")
    @Operation(summary = "Registrar proveedor (solo Gerente)")
    public ResponseEntity<Proveedor> crear(@Valid @RequestBody Proveedor proveedor) {
        return ResponseEntity.status(HttpStatus.CREATED).body(proveedorRepository.save(proveedor));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('GERENTE')")
    @Operation(summary = "Actualizar proveedor (solo Gerente)")
    public ResponseEntity<Proveedor> actualizar(@PathVariable Long id,
                                                @Valid @RequestBody Proveedor datos) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado con id: " + id));
        proveedor.setNombre(datos.getNombre());
        proveedor.setRut(datos.getRut());
        proveedor.setEmail(datos.getEmail());
        proveedor.setTelefono(datos.getTelefono());
        proveedor.setActivo(datos.getActivo());
        return ResponseEntity.ok(proveedorRepository.save(proveedor));
    }
}