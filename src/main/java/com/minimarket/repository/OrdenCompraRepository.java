package com.minimarket.repository;

import com.minimarket.entity.EstadoOrdenCompra;
import com.minimarket.entity.OrdenCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrdenCompraRepository extends JpaRepository<OrdenCompra, Long> {
    List<OrdenCompra> findByEstado(EstadoOrdenCompra estado);

    boolean existsByProductoIdAndSucursalIdAndEstado(Long productoId, Long sucursalId,
                                                     EstadoOrdenCompra estado);
}