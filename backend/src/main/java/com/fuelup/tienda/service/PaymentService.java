package com.fuelup.tienda.service;

import com.fuelup.tienda.entity.*;
import com.fuelup.tienda.exception.BusinessException;
import com.fuelup.tienda.exception.ResourceNotFoundException;
import com.fuelup.tienda.repository.OrderRepository;
import com.fuelup.tienda.repository.PaymentRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    @Transactional
    public Map<String, String> createPaymentIntent(User user, Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Pedido", orderId));

        if (!order.getUser().getId().equals(user.getId()))
            throw new BusinessException("Acceso no autorizado");

        if (order.getStatus() != OrderStatus.PENDING)
            throw new BusinessException("El pedido ya fue procesado");

        try {
            long amountInCents = order.getTotal()
                .multiply(BigDecimal.valueOf(100)).longValue();

            PaymentIntent intent = PaymentIntent.create(
                PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency("eur")
                    .putMetadata("orderId", orderId.toString())
                    .putMetadata("orderNumber", order.getOrderNumber())
                    .build()
            );

            Payment payment = Payment.builder()
                .order(order)
                .stripePaymentIntentId(intent.getId())
                .amount(order.getTotal())
                .currency("eur")
                .status(PaymentStatus.PENDING)
                .build();

            paymentRepository.save(payment);

            return Map.of(
                "clientSecret", intent.getClientSecret(),
                "paymentIntentId", intent.getId()
            );
        } catch (StripeException e) {
            log.error("Error creando PaymentIntent: {}", e.getMessage());
            throw new BusinessException("Error al procesar el pago: " + e.getMessage());
        }
    }

    @Transactional
    public void handleWebhook(String paymentIntentId, String status) {
        paymentRepository.findByStripePaymentIntentId(paymentIntentId).ifPresent(payment -> {
            if ("succeeded".equals(status)) {
                payment.setStatus(PaymentStatus.COMPLETED);
                payment.setPaidAt(LocalDateTime.now());
                payment.getOrder().setStatus(OrderStatus.CONFIRMED);
                orderRepository.save(payment.getOrder());
            } else if ("payment_failed".equals(status)) {
                payment.setStatus(PaymentStatus.FAILED);
            }
            paymentRepository.save(payment);
        });
    }
}
