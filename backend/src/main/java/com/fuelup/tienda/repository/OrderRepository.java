package com.fuelup.tienda.repository;

import com.fuelup.tienda.entity.Order;
import com.fuelup.tienda.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Optional<Order> findByOrderNumber(String orderNumber);
    List<Order> findByStatus(OrderStatus status);

    @Query("SELECT SUM(o.total) FROM Order o WHERE o.status != 'CANCELLED' AND o.createdAt BETWEEN :start AND :end")
    BigDecimal getTotalRevenue(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt BETWEEN :start AND :end")
    Long countOrdersByPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
