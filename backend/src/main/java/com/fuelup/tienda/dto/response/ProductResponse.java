package com.fuelup.tienda.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private Integer stock;
    private String imageUrl;
    private String slug;
    private String brand;
    private String flavor;
    private String weight;
    private boolean active;
    private boolean featured;
    private CategoryResponse category;
    private double averageRating;
    private int reviewCount;
    private LocalDateTime createdAt;
}
