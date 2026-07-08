package com.minimarket.assembler;

import com.minimarket.controller.UsuarioController;
import com.minimarket.controller.VentaController;
import com.minimarket.entity.Venta;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class VentaModelAssembler implements RepresentationModelAssembler<Venta, EntityModel<Venta>> {

    @Override
    public EntityModel<Venta> toModel(Venta venta) {
        EntityModel<Venta> model = EntityModel.of(venta,
                linkTo(methodOn(VentaController.class).obtenerVentaPorId(venta.getId())).withSelfRel(),
                linkTo(methodOn(VentaController.class).listarVentas()).withRel("ventas"));

        if (venta.getUsuario() != null) {
            model.add(linkTo(methodOn(UsuarioController.class).obtenerUsuarioPorId(venta.getUsuario().getId())).withRel("usuario"));
        }
        return model;
    }
}