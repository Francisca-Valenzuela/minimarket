package com.minimarket.controller;

import com.minimarket.entity.Proveedor;
import com.minimarket.exception.ResourceNotFoundException;
import com.minimarket.repository.ProveedorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProveedorControllerTest {

    @Mock
    private ProveedorRepository proveedorRepository;

    @InjectMocks
    private ProveedorController proveedorController;

    private Proveedor proveedor;

    @BeforeEach
    void setUp() {
        proveedor = new Proveedor();
        proveedor.setId(1L);
        proveedor.setNombre("Distribuidora Andina SpA");
        proveedor.setRut("76.123.456-7");
        proveedor.setEmail("ventas@andina.cl");
        proveedor.setTelefono("+56223334444");
        proveedor.setActivo(true);
    }

    @Test
    void testListar_SoloActivos() {
        when(proveedorRepository.findByActivoTrue()).thenReturn(List.of(proveedor));

        var response = proveedorController.listar();

        assertEquals(1, response.getBody().size());
        assertEquals("Distribuidora Andina SpA", response.getBody().get(0).getNombre());
    }

    @Test
    void testObtener_Existente() {
        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(proveedor));

        var response = proveedorController.obtener(1L);

        assertEquals("76.123.456-7", response.getBody().getRut());
    }

    @Test
    void testObtener_NoExistente_LanzaResourceNotFound() {
        when(proveedorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> proveedorController.obtener(99L));
    }

    @Test
    void testCrear() {
        when(proveedorRepository.save(any(Proveedor.class))).thenReturn(proveedor);

        var response = proveedorController.crear(proveedor);

        assertEquals(201, response.getStatusCode().value());
        assertEquals("Distribuidora Andina SpA", response.getBody().getNombre());
    }

    @Test
    void testActualizar_Existente() {
        Proveedor datosNuevos = new Proveedor();
        datosNuevos.setNombre("Distribuidora Andina Renovada");
        datosNuevos.setRut("76.123.456-7");
        datosNuevos.setEmail("nuevo@andina.cl");
        datosNuevos.setTelefono("+56299998888");
        datosNuevos.setActivo(false);

        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(proveedor));
        when(proveedorRepository.save(any(Proveedor.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = proveedorController.actualizar(1L, datosNuevos);

        assertEquals("Distribuidora Andina Renovada", response.getBody().getNombre());
        assertFalse(response.getBody().getActivo());
    }

    @Test
    void testActualizar_NoExistente_LanzaResourceNotFound() {
        when(proveedorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> proveedorController.actualizar(99L, proveedor));
    }
}
