package com.fuelup.tienda.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Coupon {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType discountType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;

    private BigDecimal minimumOrderAmount;
    private BigDecimal maximumDiscount;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    private Integer usageLimit;
    @Builder.Default
    private Integer usageCount = 0;

    private LocalDateTime expiresAt;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum DiscountType { PERCENTAGE, FIXED }
}
