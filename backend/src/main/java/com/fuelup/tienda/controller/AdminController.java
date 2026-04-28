package com.fuelup.tienda.controller;

import com.fuelup.tienda.dto.request.ProductRequest;
import com.fuelup.tienda.dto.response.OrderResponse;
import com.fuelup.tienda.dto.response.ProductResponse;
import com.fuelup.tienda.dto.response.UserResponse;
import com.fuelup.tienda.entity.Category;
import com.fuelup.tienda.entity.OrderStatus;
import com.fuelup.tienda.entity.User;
import com.fuelup.tienda.exception.BusinessException;
import com.fuelup.tienda.exception.ResourceNotFoundException;
import com.fuelup.tienda.repository.CategoryRepository;
import com.fuelup.tienda.repository.OrderRepository;
import com.fuelup.tienda.repository.ProductRepository;
import com.fuelup.tienda.repository.UserRepository;
import com.fuelup.tienda.service.AuthService;
import com.fuelup.tienda.service.OrderService;
import com.fuelup.tienda.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Panel de administración (solo ADMIN)")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final ProductService productService;
    private final OrderService orderService;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final OrderRepository orderRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    // ===== PRODUCTOS =====

    @PostMapping("/products")
    @Operation(summary = "Crear producto")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(request));
    }

    @PutMapping("/products/{id}")
    @Operation(summary = "Editar producto")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.update(id, request));
    }

    @DeleteMapping("/products/{id}")
    @Operation(summary = "Desactivar producto")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/products/low-stock")
    @Operation(summary = "Productos con stock bajo")
    public ResponseEntity<List<ProductResponse>> getLowStock(@RequestParam(defaultValue = "5") int threshold) {
        return ResponseEntity.ok(productRepository.findLowStockProducts(threshold)
            .stream().map(productService::toResponse).toList());
    }

    // ===== CATEGORÍAS =====

    @PostMapping("/categories")
    @Operation(summary = "Crear categoría")
    public ResponseEntity<Category> createCategory(@RequestBody Category category) {
        if (categoryRepository.existsByName(category.getName()))
            throw new BusinessException("Ya existe una categoría con ese nombre");
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryRepository.save(category));
    }

    @PutMapping("/categories/{id}")
    @Operation(summary = "Editar categoría")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id, @RequestBody Category updated) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Categoría", id));
        category.setName(updated.getName());
        category.setDescription(updated.getDescription());
        category.setImageUrl(updated.getImageUrl());
        category.setActive(updated.isActive());
        return ResponseEntity.ok(categoryRepository.save(category));
    }

    // ===== PEDIDOS =====

    @GetMapping("/orders")
    @Operation(summary = "Todos los pedidos")
    public ResponseEntity<Page<OrderResponse>> getAllOrders(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(orderRepository.findAll(
            PageRequest.of(page, size, Sort.by("createdAt").descending()))
            .map(orderService::toResponse));
    }

    @PatchMapping("/orders/{id}/status")
    @Operation(summary = "Cambiar estado de un pedido")
    public ResponseEntity<OrderResponse> updateOrderStatus(
        @PathVariable Long id,
        @RequestParam OrderStatus status
    ) {
        return ResponseEntity.ok(orderService.updateStatus(id, status));
    }

    // ===== USUARIOS =====

    @GetMapping("/users")
    @Operation(summary = "Todos los usuarios")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(userRepository.findAll(
            PageRequest.of(page, size, Sort.by("createdAt").descending()))
            .map(authService::toUserResponse));
    }

    // ===== ESTADÍSTICAS =====

    @GetMapping("/stats")
    @Operation(summary = "Estadísticas generales del dashboard")
    public ResponseEntity<Map<String, Object>> getStats() {
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime now = LocalDateTime.now();

        BigDecimal revenue = orderRepository.getTotalRevenue(startOfMonth, now);
        Long ordersThisMonth = orderRepository.countOrdersByPeriod(startOfMonth, now);

        return ResponseEntity.ok(Map.of(
            "totalUsers", userRepository.count(),
            "totalOrders", orderRepository.count(),
            "ordersThisMonth", ordersThisMonth != null ? ordersThisMonth : 0,
            "revenueThisMonth", revenue != null ? revenue : BigDecimal.ZERO,
            "totalProducts", productRepository.count()
        ));
    }
}
