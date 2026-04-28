package com.fuelup.tienda.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data @Builder
public class OrderItemResponse {
    private Long productId;
    private String productName;
    private String productImage;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}
