package com.minimarket.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Modalidad de entrega elegida para la venta")
public enum TipoEntrega {
    RETIRO_TIENDA,
    DESPACHO_DOMICILIO
}