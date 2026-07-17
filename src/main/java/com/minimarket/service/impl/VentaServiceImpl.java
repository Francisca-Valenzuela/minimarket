package com.minimarket.service.impl;

import lombok.RequiredArgsConstructor;

import com.minimarket.entity.DetalleVenta;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Promocion;
import com.minimarket.entity.StockSucursal;
import com.minimarket.entity.Sucursal;
import com.minimarket.entity.TipoDescuento;
import com.minimarket.entity.TipoEntrega;
import com.minimarket.entity.Usuario;
import com.minimarket.entity.Venta;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.PromocionRepository;
import com.minimarket.repository.StockSucursalRepository;
import com.minimarket.repository.SucursalRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.repository.VentaRepository;
import com.minimarket.service.OrdenCompraService;
import com.minimarket.service.VentaService;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VentaServiceImpl implements VentaService {

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final PromocionRepository promocionRepository;
    private final UsuarioRepository usuarioRepository;
    private final SucursalRepository sucursalRepository;
    private final StockSucursalRepository stockSucursalRepository;
    private final OrdenCompraService ordenCompraService;

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

    @Override
    @Transactional
    public Venta save(Venta venta) {
        // 1. Validar que la venta tenga un usuario asignado
        if (venta.getUsuario() == null || venta.getUsuario().getId() == null) {
            throw new RuntimeException("La venta debe estar vinculada a un usuario válido");
        }
        Usuario usuario = usuarioRepository.findById(venta.getUsuario().getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        venta.setUsuario(usuario);

        // 1b. Validar que la venta tenga una sucursal asignada
        if (venta.getSucursal() == null || venta.getSucursal().getId() == null) {
            throw new RuntimeException("La venta debe estar vinculada a una sucursal válida");
        }
        Sucursal sucursal = sucursalRepository.findById(venta.getSucursal().getId())
                .orElseThrow(() -> new RuntimeException("Sucursal no encontrada"));
        venta.setSucursal(sucursal);

        // 1c. La fecha de la venta la asigna SIEMPRE el servidor
        venta.setFecha(new java.util.Date());

        // 2. Validación defensiva: una venta sin detalles no es válida
        if (venta.getDetalles() == null || venta.getDetalles().isEmpty()) {
            throw new RuntimeException("La venta debe contener al menos un detalle");
        }

        // 2b. Validar la modalidad de entrega y su dirección asociada
        validarEntrega(venta);

        // 3. Procesar cada detalle...
        BigDecimal totalVenta = BigDecimal.ZERO;

        for (DetalleVenta detalle : venta.getDetalles()) {
            Producto producto = obtenerProductoValido(detalle);

            // --- MIGRADO: el stock ahora se valida y descuenta a nivel de SUCURSAL ---
            StockSucursal stockSucursal = obtenerStockSucursalValido(producto, venta.getSucursal().getId());
            validarStockDisponible(stockSucursal, detalle.getCantidad(), producto);
            descontarStock(stockSucursal, detalle.getCantidad());

            detalle.setProducto(producto);
            detalle.setPrecio(producto.getPrecio());
            detalle.setVenta(venta);

            // 3b. Buscar y aplicar la mejor promoción vigente para este producto/sucursal
            aplicarPromocionSiCorresponde(detalle, venta.getSucursal().getId());

            BigDecimal precioUnitario = BigDecimal.valueOf(detalle.getPrecio());
            BigDecimal descuentoUnitario = BigDecimal.valueOf(detalle.getDescuentoUnitario());
            BigDecimal precioFinalUnitario = precioUnitario.subtract(descuentoUnitario);

            BigDecimal subtotal = precioFinalUnitario.multiply(BigDecimal.valueOf(detalle.getCantidad()));
            totalVenta = totalVenta.add(subtotal);
        }

        venta.setTotal(totalVenta.setScale(2, RoundingMode.HALF_UP).doubleValue());

        return ventaRepository.save(venta);
    }

    private Producto obtenerProductoValido(DetalleVenta detalle) {
        if (detalle == null || detalle.getProducto() == null || detalle.getProducto().getId() == null) {
            throw new RuntimeException("El detalle de venta no tiene un producto válido");
        }
        return productoRepository.findById(detalle.getProducto().getId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
    }

    /**
     * Busca el registro de stock específico para la combinación
     * producto + sucursal. Si no existe, significa que ese producto
     * nunca fue asignado a esa sucursal (no es lo mismo que "stock = 0").
     */
    private StockSucursal obtenerStockSucursalValido(Producto producto, Long sucursalId) {
        return stockSucursalRepository.findBySucursalIdAndProductoId(sucursalId, producto.getId())
                .orElseThrow(() -> new RuntimeException(
                        "El producto '" + producto.getNombre() + "' no tiene stock registrado en esta sucursal"));
    }

    private void validarStockDisponible(StockSucursal stockSucursal, Integer cantidadSolicitada, Producto producto) {
        if (stockSucursal.getCantidad() < cantidadSolicitada) {
            throw new RuntimeException(
                    "Stock insuficiente para el producto '" + producto.getNombre()
                            + "' en la sucursal seleccionada. Disponible: " + stockSucursal.getCantidad()
                            + ", solicitado: " + cantidadSolicitada);
        }
    }

    /**
     * Valida la coherencia entre el tipo de entrega y la dirección de despacho:
     * - DESPACHO_DOMICILIO exige una dirección no vacía.
     * - RETIRO_TIENDA no debe llevar dirección asociada (se limpia si llega igual).
     */
    private void validarEntrega(Venta venta) {
        if (venta.getTipoEntrega() == null) {
            throw new RuntimeException("Debe indicar el tipo de entrega (RETIRO_TIENDA o DESPACHO_DOMICILIO)");
        }

        if (venta.getTipoEntrega() == TipoEntrega.DESPACHO_DOMICILIO) {
            if (venta.getDireccionDespacho() == null || venta.getDireccionDespacho().isBlank()) {
                throw new RuntimeException(
                        "La dirección de despacho es obligatoria cuando el tipo de entrega es DESPACHO_DOMICILIO");
            }
        } else {
            venta.setDireccionDespacho(null);
        }
    }

    /**
     * Descuenta la cantidad vendida del stock DE LA SUCURSAL correspondiente
     * (ya no del stock global del producto). El @Version en StockSucursal
     * sigue protegiendo contra condiciones de carrera entre ventas simultáneas
     * en la misma sucursal.
     */
    private void descontarStock(StockSucursal stockSucursal, Integer cantidadVendida) {
        stockSucursal.setCantidad(stockSucursal.getCantidad() - cantidadVendida);
        try {
            stockSucursalRepository.save(stockSucursal);
        } catch (OptimisticLockingFailureException ex) {
            throw new RuntimeException(
                    "No se pudo actualizar el stock en esta sucursal porque fue modificado por otra "
                            + "operación simultánea. Intente nuevamente.", ex);
        }

        // Reposición automática (Módulo 2): una venta real es la forma principal
        // en que baja el stock, así que el gatillo también debe vivir aquí y no
        // solo en el ajuste manual de SucursalServiceImpl.ajustarStock().
        // Se evalúa sobre 'stockSucursal' (ya mutado) y no sobre el retorno de
        // save(), para no depender de que el mock/DB devuelva la misma instancia.
        if (stockSucursal.getStockMinimo() != null && stockSucursal.getCantidad() <= stockSucursal.getStockMinimo()) {
            ordenCompraService.generarOrdenAutomatica(stockSucursal);
        }
    }

    /**
     * Busca todas las promociones activas para el producto del detalle
     * (globales o específicas de la sucursal indicada), filtra las que
     * estén vigentes hoy según su rango de fechas, y aplica la que otorgue
     * el MAYOR descuento por unidad. Si no hay ninguna vigente, el detalle
     * queda con descuentoUnitario = 0.0 y promocionAplicadaId = null.
     */
    private void aplicarPromocionSiCorresponde(DetalleVenta detalle, Long sucursalId) {
        Long productoId = detalle.getProducto().getId();
        Double precioUnitario = detalle.getPrecio();
        LocalDate hoy = LocalDate.now();

        List<Promocion> candidatas = promocionRepository
                .buscarActivasParaProductoYSucursal(productoId, sucursalId);

        Double mejorDescuento = 0.0;
        Long mejorPromocionId = null;

        for (Promocion promo : candidatas) {
            if (!promo.esVigentePara(sucursalId, hoy)) {
                continue;
            }

            double descuentoCalculado = calcularDescuentoUnitario(promo, precioUnitario);

            if (descuentoCalculado > mejorDescuento) {
                mejorDescuento = descuentoCalculado;
                mejorPromocionId = promo.getId();
            }
        }

        detalle.setDescuentoUnitario(mejorDescuento);
        detalle.setPromocionAplicadaId(mejorPromocionId);
    }

    /**
     * Calcula el monto de descuento por unidad según el tipo de promoción.
     * El descuento nunca puede superar el precio unitario (evita precios negativos).
     */
    private double calcularDescuentoUnitario(Promocion promo, Double precioUnitario) {
        double descuento;

        if (promo.getTipoDescuento() == TipoDescuento.PORCENTAJE) {
            descuento = precioUnitario * (promo.getValor() / 100.0);
        } else {
            descuento = promo.getValor();
        }

        return Math.min(descuento, precioUnitario);
    }
}
