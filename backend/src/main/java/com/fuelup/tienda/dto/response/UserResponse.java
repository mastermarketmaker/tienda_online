package com.fuelup.tienda.dto.response;

import com.fuelup.tienda.entity.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data @Builder
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private Role role;
    private boolean emailVerified;
    private LocalDateTime createdAt;
}
