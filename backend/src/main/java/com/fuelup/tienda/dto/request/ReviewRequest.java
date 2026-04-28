package com.fuelup.tienda.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ReviewRequest {
    @NotNull @Min(1) @Max(5) private Integer rating;
    private String title;
    private String comment;
}
