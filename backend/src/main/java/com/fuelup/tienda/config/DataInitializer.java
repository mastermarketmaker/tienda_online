package com.fuelup.tienda.config;

import com.fuelup.tienda.entity.*;
import com.fuelup.tienda.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    @Bean
    CommandLineRunner initData(
        UserRepository userRepo,
        CategoryRepository categoryRepo,
        ProductRepository productRepo,
        CartRepository cartRepo,
        WishlistRepository wishlistRepo,
        CouponRepository couponRepo,
        PasswordEncoder passwordEncoder
    ) {
        return args -> {
            if (userRepo.count() > 0) return;

            log.info("Inicializando datos de ejemplo...");

            // Admin
            User admin = userRepo.save(User.builder()
                .name("Admin FuelUp")
                .email("admin@fuelup.es")
                .password(passwordEncoder.encode("Admin1234!"))
                .role(Role.ADMIN)
                .emailVerified(true)
                .build());
            cartRepo.save(Cart.builder().user(admin).build());
            wishlistRepo.save(Wishlist.builder().user(admin).build());

            // Categories
            Category proteinas = categoryRepo.save(Category.builder().name("Proteínas").slug("proteinas").description("Whey, caseína y más").build());
            Category creatinas = categoryRepo.save(Category.builder().name("Creatinas").slug("creatinas").description("Para fuerza y volumen").build());
            Category aminoacidos = categoryRepo.save(Category.builder().name("Aminoácidos").slug("aminoacidos").description("BCAA y EAA").build());
            Category preWorkout = categoryRepo.save(Category.builder().name("Pre-Workout").slug("pre-workout").description("Energía explosiva").build());
            Category vitaminas = categoryRepo.save(Category.builder().name("Vitaminas").slug("vitaminas").description("Salud y bienestar").build());

            // Products
            productRepo.saveAll(List.of(
                Product.builder().name("Whey Protein Pro").slug("whey-protein-pro")
                    .description("25g de proteína por servicio. Recuperación muscular máxima.").price(new BigDecimal("39.99"))
                    .stock(100).brand("FuelUp").flavor("Chocolate").weight("1kg").featured(true).category(proteinas).build(),
                Product.builder().name("Creatina Monohidrato").slug("creatina-monohidrato")
                    .description("Aumenta tu fuerza y resistencia. Micronizada.").price(new BigDecimal("24.99"))
                    .stock(80).brand("FuelUp").weight("500g").featured(true).category(creatinas).build(),
                Product.builder().name("BCAA Essential 2:1:1").slug("bcaa-essential")
                    .description("Aminoácidos de cadena ramificada para reducir la fatiga.").price(new BigDecimal("29.99"))
                    .stock(60).brand("FuelUp").flavor("Sandía").weight("300g").featured(false).category(aminoacidos).build(),
                Product.builder().name("Pre-Workout Extreme").slug("pre-workout-extreme")
                    .description("Energía explosiva con beta-alanina y cafeína.").price(new BigDecimal("34.99"))
                    .stock(50).brand("FuelUp").flavor("Frutas del bosque").weight("400g").featured(true).category(preWorkout).build(),
                Product.builder().name("Omega 3 Ultra").slug("omega-3-ultra")
                    .description("Ácidos grasos esenciales para salud cardiovascular.").price(new BigDecimal("19.99"))
                    .stock(120).brand("FuelUp").weight("90 cápsulas").featured(false).category(vitaminas).build(),
                Product.builder().name("Mass Gainer 3000").slug("mass-gainer-3000")
                    .description("Volumen y masa muscular con carbohidratos complejos.").price(new BigDecimal("44.99"))
                    .discountPrice(new BigDecimal("35.99")).stock(40).brand("FuelUp").flavor("Vainilla").weight("3kg").featured(true).category(proteinas).build()
            ));

            // Coupons
            couponRepo.saveAll(List.of(
                Coupon.builder().code("FUELUP10").discountType(Coupon.DiscountType.PERCENTAGE)
                    .discountValue(new BigDecimal("10")).minimumOrderAmount(new BigDecimal("30"))
                    .expiresAt(LocalDateTime.now().plusMonths(6)).build(),
                Coupon.builder().code("BIENVENIDO").discountType(Coupon.DiscountType.FIXED)
                    .discountValue(new BigDecimal("5")).minimumOrderAmount(new BigDecimal("20"))
                    .usageLimit(1).expiresAt(LocalDateTime.now().plusMonths(3)).build()
            ));

            log.info("✅ Datos iniciales creados. Admin: admin@fuelup.es / Admin1234!");
        };
    }
}
