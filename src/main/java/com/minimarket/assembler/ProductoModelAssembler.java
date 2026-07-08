package com.minimarket.assembler;

import com.minimarket.controller.CategoriaController;
import com.minimarket.controller.ProductoController;
import com.minimarket.entity.Producto;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Convierte una entidad {@link Producto} en un {@link EntityModel} enriquecido
 * con enlaces HATEOAS (self, colección de productos y categoría asociada),
 * permitiendo a los clientes de la API navegar entre recursos relacionados.
 */
@Component
public class ProductoModelAssembler implements RepresentationModelAssembler<Producto, EntityModel<Producto>> {

    @Override
    public EntityModel<Producto> toModel(Producto producto) {
        EntityModel<Producto> model = EntityModel.of(producto,
                linkTo(methodOn(ProductoController.class).obtenerProductoPorId(producto.getId())).withSelfRel(),
                linkTo(methodOn(ProductoController.class).listarProductos()).withRel("productos"));

        if (producto.getCategoria() != null) {
            model.add(linkTo(methodOn(CategoriaController.class)
                    .obtenerCategoriaPorId(producto.getCategoria().getId())).withRel("categoria"));
        }

        return model;
    }
}
