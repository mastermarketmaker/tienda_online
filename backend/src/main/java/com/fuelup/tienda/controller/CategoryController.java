package com.fuelup.tienda.controller;

import com.fuelup.tienda.entity.Category;
import com.fuelup.tienda.exception.ResourceNotFoundException;
import com.fuelup.tienda.repository.CategoryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categorías", description = "Categorías de productos")
public class CategoryController {

    private final CategoryRepository categoryRepository;

    @GetMapping
    @Operation(summary = "Listar todas las categorías activas")
    public ResponseEntity<List<Category>> getAll() {
        return ResponseEntity.ok(categoryRepository.findByActiveTrue());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener categoría por ID")
    public ResponseEntity<Category> getById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Categoría", id)));
    }
}
