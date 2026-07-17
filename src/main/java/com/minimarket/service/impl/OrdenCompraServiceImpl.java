package com.minimarket.service.impl;

import com.minimarket.entity.*;
import com.minimarket.exception.ResourceNotFoundException;
import com.minimarket.repository.OrdenCompraRepository;
import com.minimarket.repository.ProveedorRepository;
import com.minimarket.repository.StockSucursalRepository;
import com.minimarket.service.OrdenCompraService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrdenCompraServiceImpl implements OrdenCompraService {

    /** Cantidad a solicitar = stockMinimo * FACTOR_REPOSICION - cantidad actual */
    private static final int FACTOR_REPOSICION = 3;

    private final OrdenCompraRepository ordenCompraRepository;
    private final ProveedorRepository proveedorRepository;
    private final StockSucursalRepository stockSucursalRepository;

    public OrdenCompraServiceImpl(OrdenCompraRepository ordenCompraRepository,
                                  ProveedorRepository proveedorRepository,
                                  StockSucursalRepository stockSucursalRepository) {
        this.ordenCompraRepository = ordenCompraRepository;
        this.proveedorRepository = proveedorRepository;
        this.stockSucursalRepository = stockSucursalRepository;
    }

    @Override
    public List<OrdenCompra> listarTodas() {
        return ordenCompraRepository.findAll();
    }

    @Override
    public List<OrdenCompra> listarPorEstado(EstadoOrdenCompra estado) {
        return ordenCompraRepository.findByEstado(estado);
    }

    @Override
    public OrdenCompra obtenerPorId(Long id) {
        return ordenCompraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden de compra no encontrada con id: " + id));
    }

    @Override
    @Transactional
    public OrdenCompra cambiarEstado(Long id, EstadoOrdenCompra nuevoEstado) {
        OrdenCompra orden = obtenerPorId(id);
        orden.setEstado(nuevoEstado);
        OrdenCompra guardada = ordenCompraRepository.save(orden);

        // Al marcar RECIBIDA, ingresa el stock a la sucursal
        if (nuevoEstado == EstadoOrdenCompra.RECIBIDA) {
            stockSucursalRepository
                    .findBySucursalIdAndProductoId(orden.getSucursal().getId(),
                                                   orden.getProducto().getId())
                    .ifPresent(stock -> {
                        stock.setCantidad(stock.getCantidad() + orden.getCantidadSolicitada());
                        stockSucursalRepository.save(stock);
                    });
        }
        return guardada;
    }

    @Override
    @Transactional
    public void generarOrdenAutomatica(StockSucursal stock) {
        Long productoId = stock.getProducto().getId();
        Long sucursalId = stock.getSucursal().getId();

        // Evitar órdenes duplicadas si ya hay una pendiente
        if (ordenCompraRepository.existsByProductoIdAndSucursalIdAndEstado(
                productoId, sucursalId, EstadoOrdenCompra.PENDIENTE)) {
            return;
        }

        // Se toma el primer proveedor activo (extensible a proveedor por producto)
        Proveedor proveedor = proveedorRepository.findByActivoTrue().stream()
                .findFirst()
                .orElse(null);
        if (proveedor == null) {
            return; // sin proveedores activos no es posible generar la orden
        }

        int cantidadAPedir = Math.max(
                stock.getStockMinimo() * FACTOR_REPOSICION - stock.getCantidad(),
                stock.getStockMinimo());

        OrdenCompra orden = new OrdenCompra();
        orden.setProveedor(proveedor);
        orden.setProducto(stock.getProducto());
        orden.setSucursal(stock.getSucursal());
        orden.setCantidadSolicitada(cantidadAPedir);
        orden.setAutomatica(true);
        ordenCompraRepository.save(orden);
    }
}