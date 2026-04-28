package com.fuelup.tienda.controller;

import com.fuelup.tienda.dto.response.CartResponse;
import com.fuelup.tienda.entity.User;
import com.fuelup.tienda.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Carrito", description = "Gestión del carrito de compras")
@SecurityRequirement(name = "bearerAuth")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Ver mi carrito")
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(cartService.getCart(user));
    }

    @PostMapping("/items/{productId}")
    @Operation(summary = "Añadir producto al carrito")
    public ResponseEntity<CartResponse> addItem(
        @AuthenticationPrincipal User user,
        @PathVariable Long productId,
        @RequestParam(defaultValue = "1") int quantity
    ) {
        return ResponseEntity.ok(cartService.addItem(user, productId, quantity));
    }

    @PutMapping("/items/{itemId}")
    @Operation(summary = "Actualizar cantidad de un item")
    public ResponseEntity<CartResponse> updateItem(
        @AuthenticationPrincipal User user,
        @PathVariable Long itemId,
        @RequestParam int quantity
    ) {
        return ResponseEntity.ok(cartService.updateItem(user, itemId, quantity));
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Eliminar item del carrito")
    public ResponseEntity<CartResponse> removeItem(
        @AuthenticationPrincipal User user,
        @PathVariable Long itemId
    ) {
        return ResponseEntity.ok(cartService.removeItem(user, itemId));
    }

    @DeleteMapping
    @Operation(summary = "Vaciar carrito")
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal User user) {
        cartService.clearCart(user);
        return ResponseEntity.noContent().build();
    }
}
