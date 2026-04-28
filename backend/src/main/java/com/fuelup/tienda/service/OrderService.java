package com.fuelup.tienda.service;

import com.fuelup.tienda.dto.request.OrderRequest;
import com.fuelup.tienda.dto.response.OrderItemResponse;
import com.fuelup.tienda.dto.response.OrderResponse;
import com.fuelup.tienda.entity.*;
import com.fuelup.tienda.exception.BusinessException;
import com.fuelup.tienda.exception.ResourceNotFoundException;
import com.fuelup.tienda.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final CouponRepository couponRepository;
    private final EmailService emailService;

    @Transactional
    public OrderResponse createFromCart(User user, OrderRequest request) {
        Cart cart = cartRepository.findByUserId(user.getId())
            .orElseThrow(() -> new BusinessException("Carrito vacío"));

        if (cart.getItems().isEmpty()) throw new BusinessException("El carrito está vacío");

        BigDecimal subtotal = cart.getTotal();
        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal shipping = subtotal.compareTo(BigDecimal.valueOf(50)) >= 0
            ? BigDecimal.ZERO : BigDecimal.valueOf(4.99);
        String couponCode = null;

        if (request.getCouponCode() != null && !request.getCouponCode().isBlank()) {
            Coupon coupon = couponRepository.findByCodeAndActiveTrue(request.getCouponCode())
                .orElseThrow(() -> new BusinessException("Cupón inválido o expirado"));
            if (coupon.getExpiresAt() != null && coupon.getExpiresAt().isBefore(LocalDateTime.now()))
                throw new BusinessException("El cupón ha expirado");
            if (coupon.getUsageLimit() != null && coupon.getUsageCount() >= coupon.getUsageLimit())
                throw new BusinessException("El cupón ha alcanzado su límite de uso");
            if (coupon.getMinimumOrderAmount() != null && subtotal.compareTo(coupon.getMinimumOrderAmount()) < 0)
                throw new BusinessException("El pedido no alcanza el mínimo para este cupón");

            discount = coupon.getDiscountType() == Coupon.DiscountType.PERCENTAGE
                ? subtotal.multiply(coupon.getDiscountValue()).divide(BigDecimal.valueOf(100))
                : coupon.getDiscountValue();

            if (coupon.getMaximumDiscount() != null && discount.compareTo(coupon.getMaximumDiscount()) > 0)
                discount = coupon.getMaximumDiscount();

            coupon.setUsageCount(coupon.getUsageCount() + 1);
            couponRepository.save(coupon);
            couponCode = coupon.getCode();
        }

        BigDecimal total = subtotal.add(shipping).subtract(discount);
        if (total.compareTo(BigDecimal.ZERO) < 0) total = BigDecimal.ZERO;

        String street = request.getShippingStreet();
        String city = request.getShippingCity();
        String province = request.getShippingProvince();
        String postalCode = request.getShippingPostalCode();
        String country = request.getShippingCountry();

        if (street == null && user.getAddress() != null) {
            street = user.getAddress().getStreet();
            city = user.getAddress().getCity();
            province = user.getAddress().getProvince();
            postalCode = user.getAddress().getPostalCode();
            country = user.getAddress().getCountry();
        }

        Order order = Order.builder()
            .orderNumber(generateOrderNumber())
            .user(user)
            .subtotal(subtotal)
            .shippingCost(shipping)
            .discount(discount)
            .total(total)
            .couponCode(couponCode)
            .shippingStreet(street)
            .shippingCity(city)
            .shippingProvince(province)
            .shippingPostalCode(postalCode)
            .shippingCountry(country)
            .notes(request.getNotes())
            .build();

        List<OrderItem> items = cart.getItems().stream().map(cartItem -> {
            Product product = cartItem.getProduct();
            if (product.getStock() < cartItem.getQuantity())
                throw new BusinessException("Stock insuficiente para: " + product.getName());
            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);

            BigDecimal unitPrice = product.getDiscountPrice() != null
                ? product.getDiscountPrice() : product.getPrice();

            return OrderItem.builder()
                .order(order).product(product)
                .quantity(cartItem.getQuantity()).unitPrice(unitPrice)
                .build();
        }).toList();

        order.setItems(items);
        Order saved = orderRepository.save(order);

        cart.getItems().clear();
        cartRepository.save(cart);

        emailService.sendOrderConfirmationEmail(user, saved);

        return toResponse(saved);
    }

    public Page<OrderResponse> getUserOrders(User user, Pageable pageable) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable).map(this::toResponse);
    }

    public OrderResponse getOrderByNumber(User user, String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
            .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));
        if (!order.getUser().getId().equals(user.getId()))
            throw new BusinessException("Acceso no autorizado a este pedido");
        return toResponse(order);
    }

    @Transactional
    public OrderResponse updateStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Pedido", orderId));
        order.setStatus(status);
        if (status == OrderStatus.SHIPPED) order.setShippedAt(LocalDateTime.now());
        if (status == OrderStatus.DELIVERED) order.setDeliveredAt(LocalDateTime.now());
        return toResponse(orderRepository.save(order));
    }

    private String generateOrderNumber() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "FU-" + date + "-" + String.format("%04d", new Random().nextInt(9999));
    }

    public OrderResponse toResponse(Order o) {
        return OrderResponse.builder()
            .id(o.getId())
            .orderNumber(o.getOrderNumber())
            .status(o.getStatus())
            .subtotal(o.getSubtotal())
            .shippingCost(o.getShippingCost())
            .discount(o.getDiscount())
            .total(o.getTotal())
            .couponCode(o.getCouponCode())
            .shippingStreet(o.getShippingStreet())
            .shippingCity(o.getShippingCity())
            .shippingPostalCode(o.getShippingPostalCode())
            .shippingCountry(o.getShippingCountry())
            .notes(o.getNotes())
            .items(o.getItems().stream().map(item -> OrderItemResponse.builder()
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .productImage(item.getProduct().getImageUrl())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(item.getSubtotal())
                .build()).toList())
            .createdAt(o.getCreatedAt())
            .shippedAt(o.getShippedAt())
            .deliveredAt(o.getDeliveredAt())
            .build();
    }
}
