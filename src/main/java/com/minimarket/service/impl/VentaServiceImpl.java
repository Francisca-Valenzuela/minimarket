package com.minimarket.service.impl;

import com.minimarket.entity.DetalleVenta;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Venta;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.VentaRepository;
import com.minimarket.service.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class VentaServiceImpl implements VentaService {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    // Métodos obligatorios requeridos por la interfaz VentaService
    @Override
    public List<Venta> findAll() {
        return ventaRepository.findAll();
    }

    @Override
    public Venta findById(Long id) {
        return ventaRepository.findById(id).orElse(null);
    }

    @Override
    public List<Venta> findByUsuarioId(Long usuarioId) {
        return ventaRepository.findByUsuarioId(usuarioId);
    }

    // Lógica robusta del método save, ajustada con la retroalimentación del docente:
    // validación defensiva de detalles, total trazable, precisión decimal,
    // separación de responsabilidades en el descuento de stock y manejo de concurrencia.
    @Override
    @Transactional // Asegura la atomicidad: si falla el stock de un producto, la venta completa se cancela
    public Venta save(Venta venta) {
        // 1. Validar que la venta tenga un usuario asignado
        if (venta.getUsuario() == null || venta.getUsuario().getId() == null) {
            throw new RuntimeException("La venta debe estar vinculada a un usuario válido");
        }

        // 2. Validación defensiva: una venta sin detalles (lista nula o vacía)
        // no es una venta válida y no debe llegar a procesarse ni a persistirse.
        if (venta.getDetalles() == null || venta.getDetalles().isEmpty()) {
            throw new RuntimeException("La venta debe contener al menos un detalle");
        }

        // 3. Procesar cada detalle: validar stock, descontar inventario y sincronizar precio.
        // Acumulamos el total con BigDecimal para evitar errores de redondeo
        // propios de la suma de Double en cálculos monetarios.
        BigDecimal totalVenta = BigDecimal.ZERO;

        for (DetalleVenta detalle : venta.getDetalles()) {
            Producto producto = obtenerProductoValido(detalle);

            validarStockDisponible(producto, detalle.getCantidad());

            // Responsabilidad de descuento de stock separada en su propio método,
            // de modo que pueda probarse y razonarse de forma independiente.
            descontarStock(producto, detalle.getCantidad());

            // Sincronizar el precio unitario del detalle con el precio actual del producto en catálogo
            detalle.setPrecio(producto.getPrecio());

            // Vincular bidireccionalmente la relación JPA
            detalle.setVenta(venta);

            BigDecimal subtotal = BigDecimal.valueOf(detalle.getPrecio())
                    .multiply(BigDecimal.valueOf(detalle.getCantidad()));
            totalVenta = totalVenta.add(subtotal);
        }

        // 4. Redondeo controlado a 2 decimales (valores monetarios en CLP/centavos)
        // para que el total quede siempre con precisión consistente.
        venta.setTotal(totalVenta.setScale(2, RoundingMode.HALF_UP).doubleValue());

        return ventaRepository.save(venta);
    }

    /**
     * Busca el producto asociado a un detalle de venta y lanza una excepción
     * de negocio clara si no existe en el catálogo.
     */
    private Producto obtenerProductoValido(DetalleVenta detalle) {
        if (detalle == null || detalle.getProducto() == null || detalle.getProducto().getId() == null) {
            throw new RuntimeException("El detalle de venta no tiene un producto válido");
        }
        return productoRepository.findById(detalle.getProducto().getId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
    }

    /**
     * Valida que el producto tenga stock suficiente para cubrir la cantidad solicitada.
     */
    private void validarStockDisponible(Producto producto, Integer cantidadSolicitada) {
        if (producto.getStock() < cantidadSolicitada) {
            throw new RuntimeException("Stock insuficiente para el producto: " + producto.getNombre());
        }
    }

    /**
     * Descuenta del stock del producto la cantidad vendida y persiste el cambio.
     * Al apoyarse en el campo @Version de Producto, Spring Data JPA aplica
     * locking optimista: si dos ventas intentan descontar el mismo producto
     * de forma simultánea, la segunda transacción en confirmar recibirá un
     * fallo de concurrencia en lugar de sobrescribir silenciosamente el stock.
     */
    private void descontarStock(Producto producto, Integer cantidadVendida) {
        producto.setStock(producto.getStock() - cantidadVendida);
        try {
            productoRepository.save(producto);
        } catch (OptimisticLockingFailureException ex) {
            throw new RuntimeException(
                    "No se pudo actualizar el stock del producto '" + producto.getNombre()
                            + "' porque fue modificado por otra operación simultánea. Intente nuevamente.", ex);
        }
    }
}