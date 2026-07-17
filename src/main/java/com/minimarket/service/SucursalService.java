package com.minimarket.service;

import com.minimarket.dto.DisponibilidadDTO;
import com.minimarket.entity.StockSucursal;
import com.minimarket.entity.Sucursal;
import java.util.List;

public interface SucursalService {
    List<Sucursal> listarTodas();
    List<Sucursal> listarActivas();
    Sucursal obtenerPorId(Long id);
    Sucursal crear(Sucursal sucursal);
    Sucursal actualizar(Long id, Sucursal sucursal);
    void eliminar(Long id);

    // Stock distribuido
    List<StockSucursal> listarStockPorSucursal(Long sucursalId);
    StockSucursal asignarStock(Long sucursalId, Long productoId, Integer cantidad, Integer stockMinimo);
    StockSucursal ajustarStock(Long sucursalId, Long productoId, Integer delta);
    List<DisponibilidadDTO> consultarDisponibilidad(Long productoId);
}