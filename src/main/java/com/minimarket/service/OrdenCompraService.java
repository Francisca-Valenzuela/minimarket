package com.minimarket.service;

import com.minimarket.entity.EstadoOrdenCompra;
import com.minimarket.entity.OrdenCompra;
import com.minimarket.entity.StockSucursal;
import java.util.List;

public interface OrdenCompraService {
    List<OrdenCompra> listarTodas();
    List<OrdenCompra> listarPorEstado(EstadoOrdenCompra estado);
    OrdenCompra obtenerPorId(Long id);
    OrdenCompra cambiarEstado(Long id, EstadoOrdenCompra nuevoEstado);
    void generarOrdenAutomatica(StockSucursal stock);
}