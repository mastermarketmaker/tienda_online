package com.fuelup.tienda.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data @Builder
public class ReviewResponse {
    private Long id;
    private String userName;
    private Integer rating;
    private String title;
    private String comment;
    private boolean verified;
    private LocalDateTime createdAt;
}
