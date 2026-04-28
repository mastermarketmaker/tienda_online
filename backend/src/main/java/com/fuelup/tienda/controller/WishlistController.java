package com.fuelup.tienda.controller;

import com.fuelup.tienda.dto.response.ProductResponse;
import com.fuelup.tienda.entity.User;
import com.fuelup.tienda.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
@Tag(name = "Wishlist", description = "Lista de deseos")
@SecurityRequirement(name = "bearerAuth")
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    @Operation(summary = "Ver mi lista de deseos")
    public ResponseEntity<List<ProductResponse>> getWishlist(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(wishlistService.getWishlist(user));
    }

    @PostMapping("/{productId}")
    @Operation(summary = "Añadir/quitar producto de la lista de deseos")
    public ResponseEntity<List<ProductResponse>> toggle(
        @AuthenticationPrincipal User user,
        @PathVariable Long productId
    ) {
        return ResponseEntity.ok(wishlistService.toggle(user, productId));
    }
}
