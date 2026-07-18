package com.minimarket.assembler;

import com.minimarket.controller.CarritoController;
import com.minimarket.controller.ProductoController;
import com.minimarket.controller.UsuarioController;
import com.minimarket.dto.CarritoResponseDTO;
import com.minimarket.entity.Carrito;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Convierte una entidad {@link Carrito} en un {@link EntityModel} de {@link CarritoResponseDTO},
 * enriquecido con enlaces HATEOAS (self, colección, producto y usuario asociados),
 * sin exponer la entidad Usuario completa.
 */
@Component
public class CarritoModelAssembler implements RepresentationModelAssembler<Carrito, EntityModel<CarritoResponseDTO>> {

    @Override
    public EntityModel<CarritoResponseDTO> toModel(Carrito carrito) {

        CarritoResponseDTO dto = new CarritoResponseDTO();
        dto.setId(carrito.getId());
        dto.setCantidad(carrito.getCantidad());

        if (carrito.getUsuario() != null) {
            dto.setUsuario(carrito.getUsuario().getNombre() + " " + carrito.getUsuario().getApellido());
        }
        if (carrito.getProducto() != null) {
            dto.setProducto(carrito.getProducto().getNombre());
        }

        EntityModel<CarritoResponseDTO> model = EntityModel.of(dto,
                linkTo(methodOn(CarritoController.class).obtenerCarritoPorId(carrito.getId())).withSelfRel(),
                linkTo(methodOn(CarritoController.class).listarCarrito()).withRel("carritos"));

        if (carrito.getProducto() != null) {
            model.add(linkTo(methodOn(ProductoController.class)
                    .obtenerProductoPorId(carrito.getProducto().getId())).withRel("producto"));
        }
        if (carrito.getUsuario() != null) {
            model.add(linkTo(methodOn(UsuarioController.class)
                    .obtenerUsuarioPorId(carrito.getUsuario().getId())).withRel("usuario"));
        }

        return model;
    }
}