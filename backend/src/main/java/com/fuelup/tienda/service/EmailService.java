package com.fuelup.tienda.service;

import com.fuelup.tienda.entity.Order;
import com.fuelup.tienda.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from:info@fuelup.es}")
    private String from;

    @Async
    public void sendWelcomeEmail(User user) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(user.getEmail());
            message.setSubject("¡Bienvenido a FuelUp! 💪");
            message.setText("""
                Hola %s,

                ¡Bienvenido a FuelUp, tu tienda de suplementos deportivos!

                Ya puedes empezar a explorar nuestro catálogo y conseguir los mejores suplementos
                para alcanzar tus objetivos.

                ¿Necesitas ayuda? Escríbenos a info@fuelup.es

                ¡A por ello!
                El equipo de FuelUp
                """.formatted(user.getName()));
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Error enviando email de bienvenida a {}: {}", user.getEmail(), e.getMessage());
        }
    }

    @Async
    public void sendOrderConfirmationEmail(User user, Order order) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(user.getEmail());
            message.setSubject("Pedido confirmado: " + order.getOrderNumber());
            message.setText("""
                Hola %s,

                ¡Tu pedido ha sido confirmado!

                Número de pedido: %s
                Total: %.2f€

                Recibirás otro email cuando tu pedido sea enviado.

                ¡Gracias por confiar en FuelUp!
                El equipo de FuelUp
                """.formatted(user.getName(), order.getOrderNumber(), order.getTotal()));
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Error enviando confirmación de pedido {}: {}", order.getOrderNumber(), e.getMessage());
        }
    }

    @Async
    public void sendPasswordResetEmail(User user, String resetToken) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(user.getEmail());
            message.setSubject("Recuperación de contraseña - FuelUp");
            message.setText("""
                Hola %s,

                Hemos recibido una solicitud para restablecer tu contraseña.

                Usa este token para restablecerla: %s

                Este enlace expirará en 1 hora. Si no solicitaste este cambio, ignora este email.

                El equipo de FuelUp
                """.formatted(user.getName(), resetToken));
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Error enviando email de reset a {}: {}", user.getEmail(), e.getMessage());
        }
    }
}
