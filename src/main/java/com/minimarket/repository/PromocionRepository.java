package com.minimarket.repository;

import com.minimarket.entity.Promocion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PromocionRepository extends JpaRepository<Promocion, Long> {

    @Query("SELECT p FROM Promocion p WHERE p.producto.id = :productoId " +
           "AND (p.sucursal IS NULL OR p.sucursal.id = :sucursalId) " +
           "AND p.activa = true")
    List<Promocion> buscarActivasParaProductoYSucursal(@Param("productoId") Long productoId,
                                                          @Param("sucursalId") Long sucursalId);
}
