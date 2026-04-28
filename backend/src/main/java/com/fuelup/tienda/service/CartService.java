package com.fuelup.tienda.service;

import com.fuelup.tienda.dto.response.CartItemResponse;
import com.fuelup.tienda.dto.response.CartResponse;
import com.fuelup.tienda.entity.Cart;
import com.fuelup.tienda.entity.CartItem;
import com.fuelup.tienda.entity.Product;
import com.fuelup.tienda.entity.User;
import com.fuelup.tienda.exception.BusinessException;
import com.fuelup.tienda.exception.ResourceNotFoundException;
import com.fuelup.tienda.repository.CartRepository;
import com.fuelup.tienda.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public CartResponse getCart(User user) {
        Cart cart = getOrCreateCart(user);
        return toResponse(cart);
    }

    @Transactional
    public CartResponse addItem(User user, Long productId, int quantity) {
        if (quantity < 1) throw new BusinessException("La cantidad mínima es 1");

        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Producto", productId));

        if (!product.isActive()) throw new BusinessException("Producto no disponible");
        if (product.getStock() < quantity) throw new BusinessException("Stock insuficiente");

        Cart cart = getOrCreateCart(user);

        Optional<CartItem> existing = cart.getItems().stream()
            .filter(i -> i.getProduct().getId().equals(productId))
            .findFirst();

        if (existing.isPresent()) {
            int newQty = existing.get().getQuantity() + quantity;
            if (product.getStock() < newQty) throw new BusinessException("Stock insuficiente");
            existing.get().setQuantity(newQty);
        } else {
            cart.getItems().add(CartItem.builder()
                .cart(cart).product(product).quantity(quantity).build());
        }

        return toResponse(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse updateItem(User user, Long itemId, int quantity) {
        Cart cart = getOrCreateCart(user);
        CartItem item = cart.getItems().stream()
            .filter(i -> i.getId().equals(itemId))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Item no encontrado en el carrito"));

        if (quantity < 1) {
            cart.getItems().remove(item);
        } else {
            if (item.getProduct().getStock() < quantity) throw new BusinessException("Stock insuficiente");
            item.setQuantity(quantity);
        }

        return toResponse(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse removeItem(User user, Long itemId) {
        Cart cart = getOrCreateCart(user);
        cart.getItems().removeIf(i -> i.getId().equals(itemId));
        return toResponse(cartRepository.save(cart));
    }

    @Transactional
    public void clearCart(User user) {
        Cart cart = getOrCreateCart(user);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUserId(user.getId())
            .orElseGet(() -> cartRepository.save(Cart.builder().user(user).build()));
    }

    private CartResponse toResponse(Cart cart) {
        return CartResponse.builder()
            .id(cart.getId())
            .items(cart.getItems().stream().map(item -> {
                BigDecimal unitPrice = item.getProduct().getDiscountPrice() != null
                    ? item.getProduct().getDiscountPrice()
                    : item.getProduct().getPrice();
                return CartItemResponse.builder()
                    .id(item.getId())
                    .productId(item.getProduct().getId())
                    .productName(item.getProduct().getName())
                    .productImage(item.getProduct().getImageUrl())
                    .unitPrice(unitPrice)
                    .quantity(item.getQuantity())
                    .subtotal(item.getSubtotal())
                    .build();
            }).toList())
            .totalItems(cart.getTotalItems())
            .total(cart.getTotal())
            .build();
    }
}
