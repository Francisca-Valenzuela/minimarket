package com.minimarket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Schema(description = "Datos requeridos para registrar o actualizar un usuario")
public class UsuarioRequestDTO {

    @NotBlank
    @Schema(description = "Nombre de usuario para login", example = "cliente")
    private String username;

    @NotBlank @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    @Schema(description = "Contraseña en texto plano (se encripta antes de guardar). En actualizaciones, se puede omitir para no cambiarla.", example = "cliente123")
    private String password;

    @NotBlank
    @Schema(example = "Francisca")
    private String nombre;

    @NotBlank
    @Schema(example = "Valenzuela")
    private String apellido;

    @NotBlank @Email
    @Schema(example = "francisca.valenzuela@minimarket.cl")
    private String email;

    @Schema(example = "Av. Siempre Viva 742, Santiago")
    private String direccion;

    @Schema(description = "Roles a asignar (opcional, solo lo puede definir un GERENTE). Si se omite, se asigna ROLE_CLIENTE por defecto.", example = "[\"ROLE_EMPLEADO\"]")
    private Set<String> roles;
}