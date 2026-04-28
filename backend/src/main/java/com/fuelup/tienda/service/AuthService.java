package com.fuelup.tienda.service;

import com.fuelup.tienda.dto.request.LoginRequest;
import com.fuelup.tienda.dto.request.RegisterRequest;
import com.fuelup.tienda.dto.response.AuthResponse;
import com.fuelup.tienda.dto.response.UserResponse;
import com.fuelup.tienda.entity.Cart;
import com.fuelup.tienda.entity.User;
import com.fuelup.tienda.entity.Wishlist;
import com.fuelup.tienda.exception.BusinessException;
import com.fuelup.tienda.repository.CartRepository;
import com.fuelup.tienda.repository.UserRepository;
import com.fuelup.tienda.repository.WishlistRepository;
import com.fuelup.tienda.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final WishlistRepository wishlistRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Ya existe una cuenta con ese email");
        }

        User user = User.builder()
            .name(request.getName())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .phone(request.getPhone())
            .verificationToken(UUID.randomUUID().toString())
            .build();

        userRepository.save(user);

        // Crear carrito y wishlist por defecto
        cartRepository.save(Cart.builder().user(user).build());
        wishlistRepository.save(Wishlist.builder().user(user).build());

        // Email de bienvenida (async)
        emailService.sendWelcomeEmail(user);

        String token = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.builder()
            .accessToken(token)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .user(toUserResponse(user))
            .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

        String token = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.builder()
            .accessToken(token)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .user(toUserResponse(user))
            .build();
    }

    public UserResponse toUserResponse(User user) {
        return UserResponse.builder()
            .id(user.getId())
            .name(user.getName())
            .email(user.getEmail())
            .phone(user.getPhone())
            .role(user.getRole())
            .emailVerified(user.isEmailVerified())
            .createdAt(user.getCreatedAt())
            .build();
    }
}
