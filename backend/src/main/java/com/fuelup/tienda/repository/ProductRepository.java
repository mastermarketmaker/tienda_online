package com.fuelup.tienda.repository;

import com.fuelup.tienda.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySlug(String slug);

    Page<Product> findByActiveTrue(Pageable pageable);

    Page<Product> findByCategoryIdAndActiveTrue(Long categoryId, Pageable pageable);

    Page<Product> findByFeaturedTrueAndActiveTrue(Pageable pageable);

    @Query("""
        SELECT p FROM Product p
        WHERE p.active = true
        AND (:categoryId IS NULL OR p.category.id = :categoryId)
        AND (:minPrice IS NULL OR p.price >= :minPrice)
        AND (:maxPrice IS NULL OR p.price <= :maxPrice)
        AND (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))
             OR LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%'))
             OR LOWER(p.brand) LIKE LOWER(CONCAT('%', :search, '%')))
        """)
    Page<Product> search(
        @Param("search") String search,
        @Param("categoryId") Long categoryId,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        Pageable pageable
    );

    @Query("SELECT p FROM Product p WHERE p.stock <= :threshold AND p.active = true")
    List<Product> findLowStockProducts(@Param("threshold") int threshold);

    boolean existsBySlug(String slug);
}
