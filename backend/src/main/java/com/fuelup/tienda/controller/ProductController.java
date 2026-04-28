package com.fuelup.tienda.controller;

import com.fuelup.tienda.dto.response.ProductResponse;
import com.fuelup.tienda.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Productos", description = "Catálogo de productos")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Listar todos los productos con paginación y filtros")
    public ResponseEntity<Page<ProductResponse>> getAll(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "12") int size,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "desc") String direction,
        @RequestParam(required = false) String search,
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) BigDecimal minPrice,
        @RequestParam(required = false) BigDecimal maxPrice
    ) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        PageRequest pageable = PageRequest.of(page, size, sort);

        if (search != null || categoryId != null || minPrice != null || maxPrice != null)
            return ResponseEntity.ok(productService.search(search, categoryId, minPrice, maxPrice, pageable));

        return ResponseEntity.ok(productService.getAll(pageable));
    }

    @GetMapping("/featured")
    @Operation(summary = "Productos destacados")
    public ResponseEntity<Page<ProductResponse>> getFeatured(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "6") int size
    ) {
        return ResponseEntity.ok(productService.getFeatured(PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener producto por ID")
    public ResponseEntity<ProductResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getById(id));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Obtener producto por slug")
    public ResponseEntity<ProductResponse> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(productService.getBySlug(slug));
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Productos por categoría")
    public ResponseEntity<Page<ProductResponse>> getByCategory(
        @PathVariable Long categoryId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "12") int size
    ) {
        return ResponseEntity.ok(productService.getByCategory(categoryId, PageRequest.of(page, size)));
    }
}
