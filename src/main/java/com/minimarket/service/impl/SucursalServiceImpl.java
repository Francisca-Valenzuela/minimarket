package com.minimarket.service.impl;

import com.minimarket.dto.DisponibilidadDTO;
import com.minimarket.entity.Producto;
import com.minimarket.entity.StockSucursal;
import com.minimarket.entity.Sucursal;
import com.minimarket.exception.InsufficientStockException;
import com.minimarket.exception.ResourceNotFoundException;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.StockSucursalRepository;
import com.minimarket.repository.SucursalRepository;
import com.minimarket.service.OrdenCompraService;
import com.minimarket.service.SucursalService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SucursalServiceImpl implements SucursalService {

    private final SucursalRepository sucursalRepository;
    private final StockSucursalRepository stockSucursalRepository;
    private final ProductoRepository productoRepository;
    private final OrdenCompraService ordenCompraService;

    public SucursalServiceImpl(SucursalRepository sucursalRepository,
                               StockSucursalRepository stockSucursalRepository,
                               ProductoRepository productoRepository,
                               OrdenCompraService ordenCompraService) {
        this.sucursalRepository = sucursalRepository;
        this.stockSucursalRepository = stockSucursalRepository;
        this.productoRepository = productoRepository;
        this.ordenCompraService = ordenCompraService;
    }

    @Override
    public List<Sucursal> listarTodas() {
        return sucursalRepository.findAll();
    }

    @Override
    public List<Sucursal> listarActivas() {
        return sucursalRepository.findByActivaTrue();
    }

    @Override
    public Sucursal obtenerPorId(Long id) {
        return sucursalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sucursal no encontrada con id: " + id));
    }

    @Override
    public Sucursal crear(Sucursal sucursal) {
        return sucursalRepository.save(sucursal);
    }

    @Override
    public Sucursal actualizar(Long id, Sucursal datos) {
        Sucursal sucursal = obtenerPorId(id);
        sucursal.setNombre(datos.getNombre());
        sucursal.setDireccion(datos.getDireccion());
        sucursal.setComuna(datos.getComuna());
        sucursal.setTelefono(datos.getTelefono());
        sucursal.setActiva(datos.getActiva());
        return sucursalRepository.save(sucursal);
    }

    @Override
    public void eliminar(Long id) {
        Sucursal sucursal = obtenerPorId(id);
        sucursal.setActiva(false); // baja lógica: preserva historial de ventas
        sucursalRepository.save(sucursal);
    }

    @Override
    public List<StockSucursal> listarStockPorSucursal(Long sucursalId) {
        obtenerPorId(sucursalId);
        return stockSucursalRepository.findBySucursalId(sucursalId);
    }

    @Override
    @Transactional
    public StockSucursal asignarStock(Long sucursalId, Long productoId,
                                      Integer cantidad, Integer stockMinimo) {
        Sucursal sucursal = obtenerPorId(sucursalId);
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + productoId));

        StockSucursal stock = stockSucursalRepository
                .findBySucursalIdAndProductoId(sucursalId, productoId)
                .orElseGet(StockSucursal::new);

        stock.setSucursal(sucursal);
        stock.setProducto(producto);
        stock.setCantidad(cantidad);
        stock.setStockMinimo(stockMinimo);
        return stockSucursalRepository.save(stock);
    }

    @Override
    @Transactional
    public StockSucursal ajustarStock(Long sucursalId, Long productoId, Integer delta) {
        StockSucursal stock = stockSucursalRepository
                .findBySucursalIdAndProductoId(sucursalId, productoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe stock del producto " + productoId + " en la sucursal " + sucursalId));

        int nuevaCantidad = stock.getCantidad() + delta;
        if (nuevaCantidad < 0) {
            throw new InsufficientStockException(
                    "Stock insuficiente para el producto " + productoId + " en la sucursal " + sucursalId);
        }
        stock.setCantidad(nuevaCantidad);
        StockSucursal guardado = stockSucursalRepository.save(stock);

        // Reposición automática (Módulo 2)
        if (guardado.getCantidad() <= guardado.getStockMinimo()) {
            ordenCompraService.generarOrdenAutomatica(guardado);
        }
        return guardado;
    }

    @Override
    public List<DisponibilidadDTO> consultarDisponibilidad(Long productoId) {
        productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + productoId));

        return stockSucursalRepository.findByProductoId(productoId).stream()
                .filter(s -> Boolean.TRUE.equals(s.getSucursal().getActiva()))
                .map(s -> {
                    DisponibilidadDTO dto = new DisponibilidadDTO();
                    dto.setSucursalId(s.getSucursal().getId());
                    dto.setSucursalNombre(s.getSucursal().getNombre());
                    dto.setProductoId(s.getProducto().getId());
                    dto.setProductoNombre(s.getProducto().getNombre());
                    dto.setCantidad(s.getCantidad());
                    dto.setDisponible(s.getCantidad() > 0);
                    return dto;
                })
                .collect(Collectors.toList());
    }
}