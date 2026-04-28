package com.fuelup.tienda.service;

import com.fuelup.tienda.dto.request.ProductRequest;
import com.fuelup.tienda.dto.response.CategoryResponse;
import com.fuelup.tienda.dto.response.ProductResponse;
import com.fuelup.tienda.entity.Category;
import com.fuelup.tienda.entity.Product;
import com.fuelup.tienda.exception.BusinessException;
import com.fuelup.tienda.exception.ResourceNotFoundException;
import com.fuelup.tienda.repository.CategoryRepository;
import com.fuelup.tienda.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Cacheable("products-featured")
    public Page<ProductResponse> getFeatured(Pageable pageable) {
        return productRepository.findByFeaturedTrueAndActiveTrue(pageable).map(this::toResponse);
    }

    public Page<ProductResponse> getAll(Pageable pageable) {
        return productRepository.findByActiveTrue(pageable).map(this::toResponse);
    }

    public Page<ProductResponse> search(String search, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        return productRepository.search(search, categoryId, minPrice, maxPrice, pageable).map(this::toResponse);
    }

    public Page<ProductResponse> getByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryIdAndActiveTrue(categoryId, pageable).map(this::toResponse);
    }

    public ProductResponse getById(Long id) {
        return toResponse(findProduct(id));
    }

    public ProductResponse getBySlug(String slug) {
        return toResponse(productRepository.findBySlug(slug)
            .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado")));
    }

    @Transactional
    @CacheEvict(value = "products-featured", allEntries = true)
    public ProductResponse create(ProductRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new ResourceNotFoundException("Categoría", request.getCategoryId()));

        String slug = generateUniqueSlug(request.getName());

        Product product = Product.builder()
            .name(request.getName())
            .description(request.getDescription())
            .price(request.getPrice())
            .discountPrice(request.getDiscountPrice())
            .stock(request.getStock())
            .imageUrl(request.getImageUrl())
            .slug(slug)
            .brand(request.getBrand())
            .flavor(request.getFlavor())
            .weight(request.getWeight())
            .featured(request.isFeatured())
            .category(category)
            .build();

        return toResponse(productRepository.save(product));
    }

    @Transactional
    @CacheEvict(value = "products-featured", allEntries = true)
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = findProduct(id);
        Category category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new ResourceNotFoundException("Categoría", request.getCategoryId()));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setDiscountPrice(request.getDiscountPrice());
        product.setStock(request.getStock());
        product.setImageUrl(request.getImageUrl());
        product.setBrand(request.getBrand());
        product.setFlavor(request.getFlavor());
        product.setWeight(request.getWeight());
        product.setFeatured(request.isFeatured());
        product.setCategory(category);

        return toResponse(productRepository.save(product));
    }

    @Transactional
    @CacheEvict(value = "products-featured", allEntries = true)
    public void delete(Long id) {
        Product product = findProduct(id);
        product.setActive(false);
        productRepository.save(product);
    }

    private Product findProduct(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Producto", id));
    }

    private String generateUniqueSlug(String name) {
        String base = Normalizer.normalize(name, Normalizer.Form.NFD)
            .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
            .toLowerCase().trim().replaceAll("[^a-z0-9]+", "-");
        String slug = base;
        int count = 1;
        while (productRepository.existsBySlug(slug)) slug = base + "-" + count++;
        return slug;
    }

    public ProductResponse toResponse(Product p) {
        return ProductResponse.builder()
            .id(p.getId())
            .name(p.getName())
            .description(p.getDescription())
            .price(p.getPrice())
            .discountPrice(p.getDiscountPrice())
            .stock(p.getStock())
            .imageUrl(p.getImageUrl())
            .slug(p.getSlug())
            .brand(p.getBrand())
            .flavor(p.getFlavor())
            .weight(p.getWeight())
            .active(p.isActive())
            .featured(p.isFeatured())
            .category(p.getCategory() != null ? CategoryResponse.builder()
                .id(p.getCategory().getId())
                .name(p.getCategory().getName())
                .slug(p.getCategory().getSlug())
                .build() : null)
            .averageRating(p.getAverageRating())
            .reviewCount(p.getReviews() != null ? p.getReviews().size() : 0)
            .createdAt(p.getCreatedAt())
            .build();
    }
}
