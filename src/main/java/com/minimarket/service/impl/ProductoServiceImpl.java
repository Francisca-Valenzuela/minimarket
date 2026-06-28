package com.minimarket.service.impl;

import com.minimarket.entity.Producto;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoServiceImpl implements ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Override
    public List<Producto> findAll() {
        return productoRepository.findAll();
    }

    @Override
    public Producto findById(Long id) {
        return productoRepository.findById(id).orElse(null);
    }

    @Override
    public Producto save(Producto producto) {
        verificarRolAdministrador();
        return productoRepository.save(producto);
    }

    @Override
    public void deleteById(Long id) {
        verificarRolAdministrador();
        productoRepository.deleteById(id);
    }

    @Override
    public List<Producto> findByCategoriaId(Long categoriaId) {
        return productoRepository.findByCategoriaId(categoriaId);
    }

    // ── Método privado de validación de rol ───────────────────────────────
    private void verificarRolAdministrador() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        boolean esAdministrador = auth != null
            && auth.getAuthorities().stream()
                   .anyMatch(a -> a.getAuthority().equals("ROLE_GERENTE")
                               || a.getAuthority().equals("ROLE_EMPLEADO"));

        if (!esAdministrador) {
            throw new SecurityException(
                "Acceso denegado: solo administradores pueden modificar productos");
        }
    }
}