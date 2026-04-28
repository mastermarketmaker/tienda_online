package com.fuelup.tienda.dto.response;

import com.fuelup.tienda.entity.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder
public class OrderResponse {
    private Long id;
    private String orderNumber;
    private OrderStatus status;
    private BigDecimal subtotal;
    private BigDecimal shippingCost;
    private BigDecimal discount;
    private BigDecimal total;
    private String couponCode;
    private List<OrderItemResponse> items;
    private String shippingStreet;
    private String shippingCity;
    private String shippingPostalCode;
    private String shippingCountry;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
}
