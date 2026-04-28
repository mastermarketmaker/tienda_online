package com.fuelup.tienda.controller;

import com.fuelup.tienda.dto.request.OrderRequest;
import com.fuelup.tienda.dto.response.OrderResponse;
import com.fuelup.tienda.entity.User;
import com.fuelup.tienda.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Pedidos", description = "Gestión de pedidos")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Crear pedido desde el carrito")
    public ResponseEntity<OrderResponse> create(
        @AuthenticationPrincipal User user,
        @RequestBody OrderRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createFromCart(user, request));
    }

    @GetMapping
    @Operation(summary = "Mis pedidos")
    public ResponseEntity<Page<OrderResponse>> getMyOrders(
        @AuthenticationPrincipal User user,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(orderService.getUserOrders(user,
            PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    @GetMapping("/{orderNumber}")
    @Operation(summary = "Detalle de un pedido")
    public ResponseEntity<OrderResponse> getOrder(
        @AuthenticationPrincipal User user,
        @PathVariable String orderNumber
    ) {
        return ResponseEntity.ok(orderService.getOrderByNumber(user, orderNumber));
    }
}
