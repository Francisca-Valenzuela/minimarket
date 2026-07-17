package com.minimarket.dto;

import java.util.List;

public class SucursalResponseDTO {

    private Long id;
    private String nombre;
    private String direccion;
    private String comuna;
    private String telefono;
    private Boolean activa;
    private List<StockSucursalResumenDTO> stocks;

    // Constructor, getters y setters
    public SucursalResponseDTO(Long id, String nombre, String direccion, String comuna,
                                 String telefono, Boolean activa, List<StockSucursalResumenDTO> stocks) {
        this.id = id;
        this.nombre = nombre;
        this.direccion = direccion;
        this.comuna = comuna;
        this.telefono = telefono;
        this.activa = activa;
        this.stocks = stocks;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDireccion() { return direccion; }
    public String getComuna() { return comuna; }
    public String getTelefono() { return telefono; }
    public Boolean getActiva() { return activa; }
    public List<StockSucursalResumenDTO> getStocks() { return stocks; }
}
