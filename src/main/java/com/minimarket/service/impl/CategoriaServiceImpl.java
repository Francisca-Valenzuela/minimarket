package com.minimarket.service.impl;

import lombok.RequiredArgsConstructor;

import com.minimarket.entity.Categoria;
import com.minimarket.repository.CategoriaRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.service.CategoriaService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;

    private final ProductoRepository productoRepository;

    @Override
    public List<Categoria> findAll() {
        return categoriaRepository.findAll();
    }

    @Override
    public Categoria findById(Long id) {
        return categoriaRepository.findById(id).orElse(null);
    }

    @Override
    public Categoria save(Categoria categoria) {
        return categoriaRepository.save(categoria);
    }

    // Validación defensiva: no se permite eliminar una categoría que aún tiene
    // productos asociados, para evitar borrados en cascada no intencionados
    // que dejarían huérfano o eliminarían silenciosamente parte del catálogo.
    @Override
    public void deleteById(Long id) {
        if (!productoRepository.findByCategoriaId(id).isEmpty()) {
            throw new RuntimeException(
                "No se puede eliminar la categoría porque tiene productos asociados. " +
                "Reasigna o elimina esos productos primero.");
        }
        categoriaRepository.deleteById(id);
    }
}
