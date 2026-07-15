package com.minimarket.security.controller;

import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.minimarket.dto.UsuarioRequestDTO;
import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.RolRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.security.model.LoginRequest;
import com.minimarket.security.util.JwtUtil;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;

    private final JwtUtil jwtUtil;

    private final UsuarioRepository usuarioRepository;

    private final RolRepository rolRepository;

    private final PasswordEncoder passwordEncoder;

    // ... (el resto de tus métodos @PostMapping("/login") y @PostMapping("/registro") quedan igual)

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Intento de inicio de sesión - usuario: {}", loginRequest.getUsername());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtil.generateToken(userDetails);

            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority -> GrantedAuthority.getAuthority())
                    .collect(Collectors.toList());

            log.info("Login exitoso - usuario: {}", loginRequest.getUsername());

            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "tipo", "Bearer",
                    "username", userDetails.getUsername(),
                    "roles", roles
            ));

        } catch (Exception e) {
            log.warn("Fallo de autenticación - usuario: {}", loginRequest.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Credenciales inválidas"));
        }
    }

    @PostMapping("/registro")
    public ResponseEntity<?> registro(@Valid @RequestBody UsuarioRequestDTO request) { // Cambiado a UsuarioRequestDTO
        log.info("Iniciando proceso de registro - usuario: {}", request.getUsername());

        if (usuarioRepository.findByUsername(request.getUsername()).isPresent()) {
            log.warn("Fallo de registro: usuario ya existe - {}", request.getUsername());
            return ResponseEntity.badRequest().body(Map.of("error", "El usuario ya existe"));
        }

        Rol rolCliente = rolRepository.findByNombre("ROLE_CLIENTE")
                .orElseGet(() -> {
                    log.info("Creando rol ROLE_CLIENTE en la base de datos.");
                    Rol nuevoRol = new Rol();
                    nuevoRol.setNombre("ROLE_CLIENTE");
                    return rolRepository.save(nuevoRol);
                });

        Set<Rol> roles = new HashSet<>();
        roles.add(rolCliente);

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setUsername(request.getUsername());
        nuevoUsuario.setPassword(passwordEncoder.encode(request.getPassword()));
        
        // Mapeo de campos requeridos para evitar excepciones de columnas nulas
        nuevoUsuario.setNombre(request.getNombre());
        nuevoUsuario.setApellido(request.getApellido());
        nuevoUsuario.setEmail(request.getEmail());
        nuevoUsuario.setDireccion(request.getDireccion());
        
        nuevoUsuario.setRoles(roles);
        usuarioRepository.save(nuevoUsuario);

        log.info("Usuario registrado exitosamente - usuario: {}", request.getUsername());
        return ResponseEntity.ok(Map.of(
                "mensaje", "Usuario registrado exitosamente",
                "username", request.getUsername(),
                "rol", "ROLE_CLIENTE"
        ));
    }
}