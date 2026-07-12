package com.minimarket.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimarket.assembler.UsuarioModelAssembler;
import com.minimarket.dto.UsuarioRequestDTO;
import com.minimarket.dto.UsuarioResponseDTO;
import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.RolRepository;
import com.minimarket.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private UsuarioModelAssembler assembler;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioController usuarioController;

    private Usuario usuario;
    private Rol rolCliente;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(usuarioController).build();
        objectMapper = new ObjectMapper();

        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("admin");

        rolCliente = new Rol();
        rolCliente.setId(1L);
        rolCliente.setNombre("ROLE_CLIENTE");

        // El assembler ahora mapea Usuario -> EntityModel<UsuarioResponseDTO>
        lenient().when(assembler.toModel(any(Usuario.class)))
                .thenAnswer(invocation -> {
                    Usuario u = invocation.getArgument(0);
                    UsuarioResponseDTO dto = new UsuarioResponseDTO();
                    dto.setId(u.getId());
                    dto.setUsername(u.getUsername());
                    return EntityModel.of(dto);
                });

        // guardarUsuario() sin roles especificados cae al default ROLE_CLIENTE
        lenient().when(passwordEncoder.encode(anyString())).thenReturn("password-encriptada");
        lenient().when(rolRepository.findByNombre("ROLE_CLIENTE")).thenReturn(Optional.of(rolCliente));
    }

    /** Payload válido reutilizable para los tests de creación/actualización. */
    private UsuarioRequestDTO buildRequest() {
        UsuarioRequestDTO request = new UsuarioRequestDTO();
        request.setUsername("admin");
        request.setPassword("password123");
        request.setNombre("Francisca");
        request.setApellido("Valenzuela");
        request.setEmail("francisca.valenzuela@minimarket.cl");
        request.setDireccion("Av. Siempre Viva 742, Santiago");
        return request;
    }

    @Test
    void testListarUsuarios() throws Exception {
        when(usuarioService.findAll()).thenReturn(List.of(usuario));
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk());
    }

    @Test
    void testObtenerUsuarioPorId_Existente() throws Exception {
        when(usuarioService.findById(1L)).thenReturn(Optional.of(usuario));
        mockMvc.perform(get("/api/usuarios/1"))
                .andExpect(status().isOk());
    }

    @Test
    void testObtenerUsuarioPorId_Inexistente() throws Exception {
        when(usuarioService.findById(1L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/usuarios/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGuardarUsuario_Exitoso() throws Exception {
        when(usuarioService.save(any(Usuario.class))).thenReturn(usuario);
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isCreated());
    }

    @Test
    void testActualizarUsuario_Existente() throws Exception {
        when(usuarioService.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioService.save(any(Usuario.class))).thenReturn(usuario);
        mockMvc.perform(put("/api/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isOk());
    }

    @Test
    void testActualizarUsuario_Inexistente() throws Exception {
        when(usuarioService.findById(1L)).thenReturn(Optional.empty());
        mockMvc.perform(put("/api/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isNotFound());
    }

    @Test
    void testEliminarUsuario_Existente() throws Exception {
        when(usuarioService.findById(1L)).thenReturn(Optional.of(usuario));
        mockMvc.perform(delete("/api/usuarios/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testEliminarUsuario_Inexistente() throws Exception {
        when(usuarioService.findById(1L)).thenReturn(Optional.empty());
        mockMvc.perform(delete("/api/usuarios/1"))
                .andExpect(status().isNotFound());
    }
}