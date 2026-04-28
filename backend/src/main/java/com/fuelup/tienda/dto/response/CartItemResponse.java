package com.fuelup.tienda.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data @Builder
public class CartItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String productImage;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal subtotal;
}
