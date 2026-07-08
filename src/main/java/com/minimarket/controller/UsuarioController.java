package com.minimarket.controller;

import com.minimarket.assembler.UsuarioModelAssembler;
import com.minimarket.dto.UsuarioRequestDTO;
import com.minimarket.dto.UsuarioResponseDTO;
import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.service.RolService;
import com.minimarket.service.UsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "Gestión de los usuarios del minimarket, incluyendo creación, actualización y eliminación.")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RolService rolService;

    @Autowired
    private UsuarioModelAssembler usuarioModelAssembler;

    @Operation(
        summary = "Listar todos los usuarios",
        description = "Retorna el listado completo de usuarios registrados, con enlaces " +
                "HATEOAS a cada usuario y a la colección. Requiere rol GERENTE."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = UsuarioResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "No autenticado"),
        @ApiResponse(responseCode = "403", description = "Sin permisos suficientes (requiere rol GERENTE)")
    })
    @GetMapping
    public CollectionModel<EntityModel<UsuarioResponseDTO>> listarUsuarios() {
        List<EntityModel<UsuarioResponseDTO>> usuarios = usuarioService.findAll().stream()
                .map(this::mapToResponseDTO)
                .map(usuarioModelAssembler::toModel)
                .collect(Collectors.toList());

        return CollectionModel.of(usuarios,
                linkTo(methodOn(UsuarioController.class).listarUsuarios()).withSelfRel());
    }

    @Operation(
        summary = "Obtener un usuario por ID",
        description = "Busca un usuario específico según su identificador y retorna sus " +
                "enlaces HATEOAS (self y colección de usuarios). Requiere rol GERENTE."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuario encontrado",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = UsuarioResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content),
        @ApiResponse(responseCode = "401", description = "No autenticado"),
        @ApiResponse(responseCode = "403", description = "Sin permisos suficientes (requiere rol GERENTE)")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<UsuarioResponseDTO>> obtenerUsuarioPorId(
            @Parameter(description = "ID del usuario", example = "1", required = true)
            @PathVariable Long id) {
        return usuarioService.findById(id)
                .map(usuario -> ResponseEntity.ok(usuarioModelAssembler.toModel(mapToResponseDTO(usuario))))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(
        summary = "Crear un nuevo usuario",
        description = "Registra un nuevo usuario con rol CLIENTE por defecto. Requiere rol GERENTE."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuario creado correctamente",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = UsuarioResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos o usuario ya existente"),
        @ApiResponse(responseCode = "401", description = "No autenticado"),
        @ApiResponse(responseCode = "403", description = "Sin permisos suficientes (requiere rol GERENTE)")
    })
    @PostMapping
    public ResponseEntity<?> guardarUsuario(
            @Parameter(description = "Datos del usuario a crear", required = true)
            @Valid @RequestBody UsuarioRequestDTO dto) {
        if (usuarioService.findByUsername(dto.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("El usuario ya existe");
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(dto.getUsername());
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));

        // Mapeo de los nuevos campos obligatorios
        usuario.setNombre(dto.getNombre());
        usuario.setApellido(dto.getApellido());
        usuario.setEmail(dto.getEmail());
        usuario.setDireccion(dto.getDireccion());

        Rol rolCliente = rolService.findByNombre("ROLE_CLIENTE")
                .orElseThrow(() -> new RuntimeException("Rol no encontrado en el sistema"));

        Set<Rol> roles = new HashSet<>();
        roles.add(rolCliente);
        usuario.setRoles(roles);

        Usuario guardado = usuarioService.save(usuario);
        EntityModel<UsuarioResponseDTO> model = usuarioModelAssembler.toModel(mapToResponseDTO(guardado));

        return ResponseEntity
                .created(linkTo(methodOn(UsuarioController.class).obtenerUsuarioPorId(guardado.getId())).toUri())
                .body(model);
    }

    @Operation(
        summary = "Actualizar un usuario existente",
        description = "Modifica los datos de un usuario ya registrado. Requiere rol GERENTE."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuario actualizado correctamente",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = UsuarioResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content),
        @ApiResponse(responseCode = "401", description = "No autenticado"),
        @ApiResponse(responseCode = "403", description = "Sin permisos suficientes (requiere rol GERENTE)")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarUsuario(
            @Parameter(description = "ID del usuario a actualizar", example = "1", required = true)
            @PathVariable Long id,
            @Parameter(description = "Nuevos datos del usuario", required = true)
            @Valid @RequestBody UsuarioRequestDTO dto) {
        Optional<Usuario> usuarioExistente = usuarioService.findById(id);
        if (usuarioExistente.isPresent()) {
            Usuario usuario = usuarioExistente.get();
            usuario.setUsername(dto.getUsername());

            // Actualización de los nuevos campos obligatorios
            usuario.setNombre(dto.getNombre());
            usuario.setApellido(dto.getApellido());
            usuario.setEmail(dto.getEmail());
            usuario.setDireccion(dto.getDireccion());

            if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
                usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
            }

            Usuario actualizado = usuarioService.save(usuario);
            return ResponseEntity.ok(usuarioModelAssembler.toModel(mapToResponseDTO(actualizado)));
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(
        summary = "Eliminar un usuario",
        description = "Elimina un usuario según su ID. Requiere rol GERENTE."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Usuario eliminado correctamente", content = @Content),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content),
        @ApiResponse(responseCode = "401", description = "No autenticado"),
        @ApiResponse(responseCode = "403", description = "Sin permisos suficientes (requiere rol GERENTE)")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(
            @Parameter(description = "ID del usuario a eliminar", example = "1", required = true)
            @PathVariable Long id) {
        Optional<Usuario> usuario = usuarioService.findById(id);
        if (usuario.isPresent()) {
            usuarioService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    private UsuarioResponseDTO mapToResponseDTO(Usuario usuario) {
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setId(usuario.getId());
        dto.setUsername(usuario.getUsername());
        if (usuario.getRoles() != null) {
            dto.setRoles(usuario.getRoles().stream()
                    .map(Rol::getNombre)
                    .collect(Collectors.toSet()));
        }
        return dto;
    }
}
