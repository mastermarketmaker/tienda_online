package com.fuelup.tienda.controller;

import com.fuelup.tienda.dto.request.ReviewRequest;
import com.fuelup.tienda.dto.response.ReviewResponse;
import com.fuelup.tienda.entity.User;
import com.fuelup.tienda.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Reseñas", description = "Reseñas de productos")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/product/{productId}")
    @Operation(summary = "Reseñas de un producto")
    public ResponseEntity<Page<ReviewResponse>> getByProduct(
        @PathVariable Long productId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(reviewService.getByProduct(productId,
            PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    @PostMapping("/product/{productId}")
    @Operation(summary = "Escribir una reseña")
    public ResponseEntity<ReviewResponse> create(
        @AuthenticationPrincipal User user,
        @PathVariable Long productId,
        @Valid @RequestBody ReviewRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.create(user, productId, request));
    }

    @DeleteMapping("/{reviewId}")
    @Operation(summary = "Eliminar mi reseña")
    public ResponseEntity<Void> delete(
        @AuthenticationPrincipal User user,
        @PathVariable Long reviewId
    ) {
        reviewService.delete(user, reviewId);
        return ResponseEntity.noContent().build();
    }
}
