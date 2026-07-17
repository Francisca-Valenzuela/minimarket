package com.minimarket.assembler;

import com.minimarket.controller.UsuarioController;
import com.minimarket.controller.VentaController;
import com.minimarket.dto.DetalleVentaResponseDTO;
import com.minimarket.dto.VentaResponseDTO;
import com.minimarket.entity.Venta;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class VentaModelAssembler implements RepresentationModelAssembler<Venta, EntityModel<VentaResponseDTO>> {

    @Override
    public EntityModel<VentaResponseDTO> toModel(Venta venta) {

        // Mapeamos la Entidad al DTO
        VentaResponseDTO dto = new VentaResponseDTO();
        dto.setId(venta.getId());
        dto.setFecha(venta.getFecha());
        dto.setTotal(venta.getTotal());
        dto.setTipoEntrega(venta.getTipoEntrega());
        dto.setDireccionDespacho(venta.getDireccionDespacho());

        // Extraemos solo el nombre del comprador (Protegiendo sus otros datos)
        if (venta.getUsuario() != null) {
            dto.setComprador(venta.getUsuario().getNombre() + " " + venta.getUsuario().getApellido());
        }

        // --- NUEVO: extraemos solo el nombre de la sucursal (sin exponer toda la entidad) ---
        if (venta.getSucursal() != null) {
            dto.setSucursal(venta.getSucursal().getNombre());
        }

        // Mapeamos los detalles a DTOs planos
        if (venta.getDetalles() != null) {
            dto.setDetalles(venta.getDetalles().stream().map(d -> {
                DetalleVentaResponseDTO detDto = new DetalleVentaResponseDTO();
                detDto.setId(d.getId());
                detDto.setProducto(d.getProducto() != null ? d.getProducto().getNombre() : "Desconocido");
                detDto.setCantidad(d.getCantidad());
                detDto.setPrecio(d.getPrecio());
                return detDto;
            }).collect(Collectors.toList()));
        }

        // Agregamos HATEOAS al DTO
        EntityModel<VentaResponseDTO> model = EntityModel.of(dto,
                linkTo(methodOn(VentaController.class).obtenerVentaPorId(venta.getId())).withSelfRel(),
                linkTo(methodOn(VentaController.class).listarVentas()).withRel("ventas"));

        if (venta.getUsuario() != null) {
            model.add(linkTo(methodOn(UsuarioController.class).obtenerUsuarioPorId(venta.getUsuario().getId())).withRel("usuario"));
        }
        return model;
    }
}
