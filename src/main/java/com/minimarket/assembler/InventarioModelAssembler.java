package com.minimarket.assembler;

import com.minimarket.controller.InventarioController;
import com.minimarket.controller.ProductoController;
import com.minimarket.entity.Inventario;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Convierte una entidad {@link Inventario} en un {@link EntityModel} enriquecido
 * con enlaces HATEOAS (self, colección de movimientos y producto afectado por el movimiento).
 */
@Component
public class InventarioModelAssembler implements RepresentationModelAssembler<Inventario, EntityModel<Inventario>> {

    @Override
    public EntityModel<Inventario> toModel(Inventario inventario) {
        EntityModel<Inventario> model = EntityModel.of(inventario,
                linkTo(methodOn(InventarioController.class).obtenerInventarioPorId(inventario.getId())).withSelfRel(),
                linkTo(methodOn(InventarioController.class).listarInventario()).withRel("movimientos-inventario"));

        if (inventario.getProducto() != null) {
            model.add(linkTo(methodOn(ProductoController.class)
                    .obtenerProductoPorId(inventario.getProducto().getId())).withRel("producto"));
        }

        return model;
    }
}
