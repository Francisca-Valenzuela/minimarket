package com.minimarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Orden de compra expuesta al cliente, sin referencias circulares")
public class OrdenCompraResponseDTO {

    private Long id;
    private ProveedorResumenDTO proveedor;
    private ProductoResumenDTO producto;
    private SucursalResumenDTO sucursal;
    private Integer cantidadSolicitada;
    private LocalDateTime fechaCreacion;
    private String estado;
    private Boolean automatica;

    public OrdenCompraResponseDTO(Long id, ProveedorResumenDTO proveedor, ProductoResumenDTO producto,
                                    SucursalResumenDTO sucursal, Integer cantidadSolicitada,
                                    LocalDateTime fechaCreacion, String estado, Boolean automatica) {
        this.id = id;
        this.proveedor = proveedor;
        this.producto = producto;
        this.sucursal = sucursal;
        this.cantidadSolicitada = cantidadSolicitada;
        this.fechaCreacion = fechaCreacion;
        this.estado = estado;
        this.automatica = automatica;
    }

    public Long getId() { return id; }
    public ProveedorResumenDTO getProveedor() { return proveedor; }
    public ProductoResumenDTO getProducto() { return producto; }
    public SucursalResumenDTO getSucursal() { return sucursal; }
    public Integer getCantidadSolicitada() { return cantidadSolicitada; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public String getEstado() { return estado; }
    public Boolean getAutomatica() { return automatica; }
}
