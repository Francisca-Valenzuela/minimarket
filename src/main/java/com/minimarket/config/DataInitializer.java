package com.minimarket.config;

import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.RolRepository;
import com.minimarket.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(RolRepository rolRepository,
                                      UsuarioRepository usuarioRepository,
                                      PasswordEncoder passwordEncoder) {
        return args -> {
            // Crear roles si no existen
            Rol rolGerente = rolRepository.findByNombre("ROLE_GERENTE")
                    .orElseGet(() -> rolRepository.save(crearRol("ROLE_GERENTE")));
            Rol rolEmpleado = rolRepository.findByNombre("ROLE_EMPLEADO")
                    .orElseGet(() -> rolRepository.save(crearRol("ROLE_EMPLEADO")));
            Rol rolCliente = rolRepository.findByNombre("ROLE_CLIENTE")
                    .orElseGet(() -> rolRepository.save(crearRol("ROLE_CLIENTE")));
            // Roles usados por SucursalController/ProveedorController/OrdenCompraController
            // (@PreAuthorize hasAnyRole('GERENTE','JEFE_TURNO','REPONEDOR')) que antes
            // nunca se creaban, dejando esos accesos inalcanzables salvo para GERENTE.
            Rol rolJefeTurno = rolRepository.findByNombre("ROLE_JEFE_TURNO")
                    .orElseGet(() -> rolRepository.save(crearRol("ROLE_JEFE_TURNO")));
            Rol rolReponedor = rolRepository.findByNombre("ROLE_REPONEDOR")
                    .orElseGet(() -> rolRepository.save(crearRol("ROLE_REPONEDOR")));

            // Crear usuario gerente de prueba
            if (usuarioRepository.findByUsername("gerente").isEmpty()) {
                Usuario gerente = new Usuario();
                gerente.setUsername("gerente");
                gerente.setPassword(passwordEncoder.encode("gerente123"));
                gerente.setNombre("Carlos");
                gerente.setApellido("Ramírez");
                gerente.setEmail("gerente@minimarket.cl");
                gerente.setDireccion("Av. Providencia 1234, Santiago");
                Set<Rol> roles = new HashSet<>();
                roles.add(rolGerente);
                gerente.setRoles(roles);
                usuarioRepository.save(gerente);
            }

            // Crear usuario empleado de prueba
            if (usuarioRepository.findByUsername("empleado").isEmpty()) {
                Usuario empleado = new Usuario();
                empleado.setUsername("empleado");
                empleado.setPassword(passwordEncoder.encode("empleado123"));
                empleado.setNombre("Ana");
                empleado.setApellido("González");
                empleado.setEmail("empleado@minimarket.cl");
                empleado.setDireccion("Calle Merced 456, Santiago");
                Set<Rol> roles = new HashSet<>();
                roles.add(rolEmpleado);
                empleado.setRoles(roles);
                usuarioRepository.save(empleado);
            }

            // Crear usuario cliente de prueba
            if (usuarioRepository.findByUsername("cliente").isEmpty()) {
                Usuario cliente = new Usuario();
                cliente.setUsername("cliente");
                cliente.setPassword(passwordEncoder.encode("cliente123"));
                cliente.setNombre("Pedro");
                cliente.setApellido("Soto");
                cliente.setEmail("cliente@minimarket.cl");
                cliente.setDireccion("Pasaje Los Aromos 789, Santiago");
                Set<Rol> roles = new HashSet<>();
                roles.add(rolCliente);
                cliente.setRoles(roles);
                usuarioRepository.save(cliente);
            }

            // Crear usuario jefe de turno de prueba
            if (usuarioRepository.findByUsername("jefeturno").isEmpty()) {
                Usuario jefeTurno = new Usuario();
                jefeTurno.setUsername("jefeturno");
                jefeTurno.setPassword(passwordEncoder.encode("jefeturno123"));
                jefeTurno.setNombre("Marcela");
                jefeTurno.setApellido("Rojas");
                jefeTurno.setEmail("jefeturno@minimarket.cl");
                jefeTurno.setDireccion("Av. Vicuña Mackenna 2200, Santiago");
                Set<Rol> roles = new HashSet<>();
                roles.add(rolJefeTurno);
                jefeTurno.setRoles(roles);
                usuarioRepository.save(jefeTurno);
            }

            // Crear usuario reponedor de prueba
            if (usuarioRepository.findByUsername("reponedor").isEmpty()) {
                Usuario reponedor = new Usuario();
                reponedor.setUsername("reponedor");
                reponedor.setPassword(passwordEncoder.encode("reponedor123"));
                reponedor.setNombre("Luis");
                reponedor.setApellido("Muñoz");
                reponedor.setEmail("reponedor@minimarket.cl");
                reponedor.setDireccion("Calle San Diego 890, Santiago");
                Set<Rol> roles = new HashSet<>();
                roles.add(rolReponedor);
                reponedor.setRoles(roles);
                usuarioRepository.save(reponedor);
            }

            System.out.println("Datos de prueba cargados: gerente/empleado/cliente/jefeturno/reponedor");
        };
    }

    private Rol crearRol(String nombre) {
        Rol rol = new Rol();
        rol.setNombre(nombre);
        return rol;
    }
}