package com.fuelup.tienda.controller;

import com.fuelup.tienda.entity.User;
import com.fuelup.tienda.service.PaymentService;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Pagos", description = "Integración con Stripe")
public class PaymentController {

    private final PaymentService paymentService;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @PostMapping("/create-intent/{orderId}")
    @Operation(summary = "Crear PaymentIntent de Stripe para un pedido")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, String>> createIntent(
        @AuthenticationPrincipal User user,
        @PathVariable Long orderId
    ) {
        return ResponseEntity.ok(paymentService.createPaymentIntent(user, orderId));
    }

    @PostMapping("/webhook")
    @Operation(summary = "Webhook de Stripe (sin autenticación)")
    public ResponseEntity<String> handleWebhook(
        @RequestBody String payload,
        @RequestHeader("Stripe-Signature") String sigHeader
    ) {
        try {
            Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            if (event.getType().startsWith("payment_intent.")) {
                PaymentIntent intent = (PaymentIntent) event.getDataObjectDeserializer()
                    .getObject().orElse(null);
                if (intent != null)
                    paymentService.handleWebhook(intent.getId(), event.getType().replace("payment_intent.", ""));
            }
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            log.error("Webhook error: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Webhook error");
        }
    }
}
