package com.fuelup.tienda.service;

import com.fuelup.tienda.dto.response.ProductResponse;
import com.fuelup.tienda.entity.Product;
import com.fuelup.tienda.entity.User;
import com.fuelup.tienda.entity.Wishlist;
import com.fuelup.tienda.exception.ResourceNotFoundException;
import com.fuelup.tienda.repository.ProductRepository;
import com.fuelup.tienda.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;

    public List<ProductResponse> getWishlist(User user) {
        Wishlist wishlist = getOrCreate(user);
        return wishlist.getProducts().stream().map(productService::toResponse).toList();
    }

    @Transactional
    public List<ProductResponse> toggle(User user, Long productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Producto", productId));
        Wishlist wishlist = getOrCreate(user);

        if (wishlist.getProducts().contains(product))
            wishlist.getProducts().remove(product);
        else
            wishlist.getProducts().add(product);

        wishlistRepository.save(wishlist);
        return wishlist.getProducts().stream().map(productService::toResponse).toList();
    }

    private Wishlist getOrCreate(User user) {
        return wishlistRepository.findByUserId(user.getId())
            .orElseGet(() -> wishlistRepository.save(Wishlist.builder().user(user).build()));
    }
}
