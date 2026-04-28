package com.fuelup.tienda.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {
    @NotBlank private String name;
    private String description;
    @NotNull @Positive private BigDecimal price;
    private BigDecimal discountPrice;
    @NotNull @Min(0) private Integer stock;
    private String imageUrl;
    private String brand;
    private String flavor;
    private String weight;
    private boolean featured;
    @NotNull private Long categoryId;
}
