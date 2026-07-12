package com.minimarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import java.util.List;

@Schema(description = "Objeto de transferencia que representa una Venta, sin exponer datos internos de la BD")
public class VentaResponseDTO {
    
    @Schema(description = "ID de la venta", example = "1")
    private Long id;
    
    @Schema(description = "Fecha en la que se realizó la transacción")
    private Date fecha;
    
    @Schema(description = "Monto total de la venta", example = "2400.0")
    private Double total;
    
    @Schema(description = "Nombre completo del comprador", example = "Pedro Soto")
    private String comprador;
    
    @Schema(description = "Lista plana de los productos comprados")
    private List<DetalleVentaResponseDTO> detalles;

    // Getters y Setters
    public Long getId() { 
        return id; 
    }
    
    public void setId(Long id) { 
        this.id = id; 
    }

    public Date getFecha() { 
        return fecha; 
    }

    public void setFecha(Date fecha) { 
        this.fecha = fecha; 
    }

    public Double getTotal() { 
        return total; 
    }

    public void setTotal(Double total) { 
        this.total = total; 
    }

    public String getComprador() { 
        return comprador; 
    }

    public void setComprador(String comprador) { 
        this.comprador = comprador; 
    }

    public List<DetalleVentaResponseDTO> getDetalles() { 
        return detalles; 
    }

    public void setDetalles(List<DetalleVentaResponseDTO> detalles) { 
        this.detalles = detalles; 
    }
}