package com.minimarket.controller;

import com.minimarket.assembler.UsuarioModelAssembler;
import com.minimarket.dto.UsuarioRequestDTO;
import com.minimarket.dto.UsuarioResponseDTO;
import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.RolRepository;
import com.minimarket.service.UsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/api/usuarios", produces = { "application/hal+json", MediaType.APPLICATION_JSON_VALUE })
@Tag(name = "Usuarios", description = "Gestión de cuentas y perfiles de usuario del sistema.")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioModelAssembler assembler;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Operation(summary = "Listar todos los usuarios", description = "Devuelve una lista con todos los usuarios registrados y sus enlaces HATEOAS.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente",
            content = @Content(mediaType = "application/hal+json",
                array = @ArraySchema(schema = @Schema(implementation = UsuarioResponseDTO.class)))),
        @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Sin permisos suficientes", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @GetMapping
    @PreAuthorize("hasRole('GERENTE')")
    public CollectionModel<EntityModel<UsuarioResponseDTO>> listarUsuarios() {
        List<EntityModel<UsuarioResponseDTO>> usuarios = usuarioService.findAll().stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(usuarios, linkTo(methodOn(UsuarioController.class).listarUsuarios()).withSelfRel());
    }

    @Operation(summary = "Obtener usuario por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Usuario encontrado",
            content = @Content(mediaType = "application/hal+json", schema = @Schema(implementation = UsuarioResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Sin permisos", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('GERENTE', 'EMPLEADO')")
    public ResponseEntity<EntityModel<UsuarioResponseDTO>> obtenerUsuarioPorId(
            @Parameter(description = "Identificador único del usuario", example = "1", required = true)
            @PathVariable Long id) {

        Optional<Usuario> usuario = usuarioService.findById(id);
        if (usuario.isEmpty()) return ResponseEntity.notFound().build();

        return ResponseEntity.ok(assembler.toModel(usuario.get()));
    }

    @Operation(summary = "Registrar nuevo usuario", description = "Solo un GERENTE puede crear usuarios directamente (empleados u otros gerentes). Si no se especifican roles, se asigna ROLE_CLIENTE por defecto.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente",
            content = @Content(mediaType = "application/hal+json", schema = @Schema(implementation = UsuarioResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Sin permisos suficientes", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @PostMapping
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<EntityModel<UsuarioResponseDTO>> guardarUsuario(
            @Parameter(description = "Datos del usuario a crear", required = true)
            @Valid @RequestBody UsuarioRequestDTO request) {

        Usuario usuario = new Usuario();
        usuario.setUsername(request.getUsername());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());
        usuario.setEmail(request.getEmail());
        usuario.setDireccion(request.getDireccion());
        usuario.setRoles(resolveRoles(request.getRoles()));

        Usuario guardado = usuarioService.save(usuario);
        return ResponseEntity
                .created(linkTo(methodOn(UsuarioController.class).obtenerUsuarioPorId(guardado.getId())).toUri())
                .body(assembler.toModel(guardado));
    }

    @Operation(summary = "Actualizar usuario", description = "La contraseña y los roles son opcionales: si se omiten, se conservan los valores actuales.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Usuario actualizado correctamente",
            content = @Content(mediaType = "application/hal+json", schema = @Schema(implementation = UsuarioResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Sin permisos", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<EntityModel<UsuarioResponseDTO>> actualizarUsuario(
            @Parameter(description = "ID del usuario a modificar", example = "1", required = true)
            @PathVariable Long id,
            @Parameter(description = "Nuevos datos del usuario", required = true)
            @Valid @RequestBody UsuarioRequestDTO request) {

        Optional<Usuario> existenteOpt = usuarioService.findById(id);
        if (existenteOpt.isEmpty()) return ResponseEntity.notFound().build();

        Usuario existente = existenteOpt.get();
        existente.setUsername(request.getUsername());
        existente.setNombre(request.getNombre());
        existente.setApellido(request.getApellido());
        existente.setEmail(request.getEmail());
        existente.setDireccion(request.getDireccion());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            existente.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            existente.setRoles(resolveRoles(request.getRoles()));
        }

        return ResponseEntity.ok(assembler.toModel(usuarioService.save(existente)));
    }

    @Operation(summary = "Eliminar usuario")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Usuario eliminado correctamente"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "403", description = "Sin permisos suficientes", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('GERENTE')")
    public ResponseEntity<Void> eliminarUsuario(
            @Parameter(description = "ID del usuario a eliminar", example = "1", required = true)
            @PathVariable Long id) {

        Optional<Usuario> existente = usuarioService.findById(id);
        if (existente.isPresent()) {
            usuarioService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Resuelve nombres de rol (ej. "ROLE_EMPLEADO") a entidades Rol existentes.
     * Si no se especifica ninguno, asigna ROLE_CLIENTE por defecto.
     */
    private Set<Rol> resolveRoles(Set<String> nombresRoles) {
        Set<String> nombres = (nombresRoles == null || nombresRoles.isEmpty())
                ? Set.of("ROLE_CLIENTE")
                : nombresRoles;

        return nombres.stream()
                .map(nombre -> rolRepository.findByNombre(nombre)
                        .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado: " + nombre)))
                .collect(Collectors.toSet());
    }
}