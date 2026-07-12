package com.minimarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import java.util.Set;

@Getter
@Setter
@Schema(description = "Representación pública de un usuario, sin datos sensibles ni de contacto")
public class UsuarioResponseDTO {

    @Schema(description = "Identificador único", example = "1")
    private Long id;

    @Schema(description = "Nombre de usuario", example = "gerente")
    private String username;

    @Schema(description = "Roles asignados", example = "[\"ROLE_GERENTE\"]")
    private Set<String> roles;
}
