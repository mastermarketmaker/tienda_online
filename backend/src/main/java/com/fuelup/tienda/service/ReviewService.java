package com.fuelup.tienda.service;

import com.fuelup.tienda.dto.request.ReviewRequest;
import com.fuelup.tienda.dto.response.ReviewResponse;
import com.fuelup.tienda.entity.Product;
import com.fuelup.tienda.entity.Review;
import com.fuelup.tienda.entity.User;
import com.fuelup.tienda.exception.BusinessException;
import com.fuelup.tienda.exception.ResourceNotFoundException;
import com.fuelup.tienda.repository.ProductRepository;
import com.fuelup.tienda.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;

    public Page<ReviewResponse> getByProduct(Long productId, Pageable pageable) {
        return reviewRepository.findByProductId(productId, pageable).map(this::toResponse);
    }

    @Transactional
    public ReviewResponse create(User user, Long productId, ReviewRequest request) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Producto", productId));

        if (reviewRepository.existsByUserIdAndProductId(user.getId(), productId))
            throw new BusinessException("Ya has escrito una reseña para este producto");

        Review review = Review.builder()
            .user(user).product(product)
            .rating(request.getRating())
            .title(request.getTitle())
            .comment(request.getComment())
            .build();

        return toResponse(reviewRepository.save(review));
    }

    @Transactional
    public void delete(User user, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ResourceNotFoundException("Reseña", reviewId));
        if (!review.getUser().getId().equals(user.getId()))
            throw new BusinessException("No puedes eliminar esta reseña");
        reviewRepository.delete(review);
    }

    private ReviewResponse toResponse(Review r) {
        return ReviewResponse.builder()
            .id(r.getId())
            .userName(r.getUser().getName())
            .rating(r.getRating())
            .title(r.getTitle())
            .comment(r.getComment())
            .verified(r.isVerified())
            .createdAt(r.getCreatedAt())
            .build();
    }
}
