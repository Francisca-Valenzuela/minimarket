package com.minimarket.repository;

import com.minimarket.dto.RotacionProductoDTO;
import com.minimarket.entity.DetalleVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {

    // ----------------------------------------------------------------
    // Consultas derivadas (Spring Data JPA)
    // ----------------------------------------------------------------

    /**
     * Retorna todos los detalles asociados a una venta específica.
     * Usado por VentaService y VentaController para mostrar el
     * desglose de una venta al consultarla por ID.
     *
     * @param ventaId ID de la venta
     * @return Lista de DetalleVenta pertenecientes a esa venta
     */
    List<DetalleVenta> findByVentaId(Long ventaId);

    /**
     * Retorna todos los detalles que incluyen un producto específico.
     * Útil para auditoría: saber en qué ventas apareció un producto,
     * complementando el registro de Inventario (Entrada/Salida).
     *
     * @param productoId ID del producto
     * @return Lista de DetalleVenta que contienen ese producto
     */
    List<DetalleVenta> findByProductoId(Long productoId);

    /**
     * Retorna todos los detalles de ventas realizadas por un usuario.
     * Permite a ROLE_GERENTE o ROLE_EMPLEADO consultar el historial
     * de compras de un cliente específico.
     *
     * @param usuarioId ID del usuario (cliente)
     * @return Lista de DetalleVenta asociados a ese usuario
     */
    List<DetalleVenta> findByVenta_UsuarioId(Long usuarioId);

    // ----------------------------------------------------------------
    // Reporte de rotación de productos por período
    // ----------------------------------------------------------------

    /**
     * Calcula la rotación de productos en un rango de fechas.
     *
     * - Agrupa por producto (id + nombre).
     * - Suma las unidades vendidas  → SUM(d.cantidad).
     * - Suma los ingresos generados → SUM(d.cantidad * d.precio).
     *   NOTA: usa d.precio (precio capturado al momento de la venta),
     *   NO d.producto.precio, para respetar la trazabilidad histórica.
     * - Ordena de mayor a menor rotación (más vendido primero).
     *
     * Compatible con java.util.Date usado en la entidad Venta.
     *
     * @param fechaInicio Fecha de inicio del período (inclusive)
     * @param fechaFin    Fecha de fin del período (inclusive)
     * @return Lista de RotacionProductoDTO ordenada por unidades vendidas DESC
     */
    @Query("""
        SELECT new com.minimarket.dto.RotacionProductoDTO(
            d.producto.id,
            d.producto.nombre,
            SUM(d.cantidad),
            SUM(d.cantidad * d.precio)
        )
        FROM DetalleVenta d
        JOIN d.venta v
        WHERE v.fecha BETWEEN :fechaInicio AND :fechaFin
        GROUP BY d.producto.id, d.producto.nombre
        ORDER BY SUM(d.cantidad) DESC
    """)
    List<RotacionProductoDTO> findRotacionByPeriodo(
        @Param("fechaInicio") Date fechaInicio,
        @Param("fechaFin")    Date fechaFin
    );
}