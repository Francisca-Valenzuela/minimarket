package com.minimarket.service.impl;

import com.minimarket.entity.Inventario;
import com.minimarket.repository.InventarioRepository;
import com.minimarket.service.InventarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class InventarioServiceImpl implements InventarioService {

    // Únicos tipos de movimiento aceptados por el sistema (Entrada o Salida de stock).
    private static final Set<String> TIPOS_MOVIMIENTO_VALIDOS = Set.of("Entrada", "Salida");

    @Autowired
    private InventarioRepository inventarioRepository;

    @Override
    public List<Inventario> findAll() {
        return inventarioRepository.findAll();
    }

    @Override
    public Inventario findById(Long id) {
        return inventarioRepository.findById(id).orElse(null);
    }

    // Validación defensiva del movimiento de inventario (retroalimentación docente S6):
    // se rechazan movimientos sin producto válido, con tipo de movimiento distinto
    // de "Entrada"/"Salida", o con cantidad nula, cero o negativa, antes de persistir.
    @Override
    public Inventario save(Inventario inventario) {
        validarProducto(inventario);
        validarTipoMovimiento(inventario);
        validarCantidad(inventario);
        return inventarioRepository.save(inventario);
    }

    private void validarProducto(Inventario inventario) {
        if (inventario.getProducto() == null || inventario.getProducto().getId() == null) {
            throw new RuntimeException("El movimiento de inventario debe estar asociado a un producto válido");
        }
    }

    private void validarTipoMovimiento(Inventario inventario) {
        String tipo = inventario.getTipoMovimiento();
        if (tipo == null || tipo.isBlank() || !TIPOS_MOVIMIENTO_VALIDOS.contains(tipo)) {
            throw new RuntimeException(
                "Tipo de movimiento inválido: '" + tipo + "'. Valores permitidos: " + TIPOS_MOVIMIENTO_VALIDOS);
        }
    }

    private void validarCantidad(Inventario inventario) {
        Integer cantidad = inventario.getCantidad();
        if (cantidad == null || cantidad <= 0) {
            throw new RuntimeException("La cantidad del movimiento debe ser un valor positivo mayor a cero");
        }
    }

    @Override
    public void deleteById(Long id) {
        inventarioRepository.deleteById(id);
    }

    @Override
    public List<Inventario> findByProductoId(Long productoId) {
        return inventarioRepository.findByProductoId(productoId);
    }
}
