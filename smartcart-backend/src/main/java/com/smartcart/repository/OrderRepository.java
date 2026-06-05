package com.smartcart.repository;

import com.smartcart.entity.Order;
import com.smartcart.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.status = 'PENDING' AND o.createdAt <= :threshold")
    List<Order> findPendingOrdersBefore(LocalDateTime threshold);

    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Order> findByIdAndUserId(Long id, Long userId);

    Optional<Order> findByOrderNumber(String orderNumber);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    long countByStatus(OrderStatus status);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status != 'CANCELLED'")
    BigDecimal getTotalRevenue();

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.createdAt >= :since AND o.status != 'CANCELLED'")
    BigDecimal getRevenueSince(LocalDateTime since);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :since")
    long countOrdersSince(LocalDateTime since);

    @Query(value = "SELECT CAST(o.created_at AS DATE) AS day, SUM(o.total_amount) AS value " +
           "FROM orders o WHERE o.created_at >= :since AND o.status != 'CANCELLED' " +
           "GROUP BY CAST(o.created_at AS DATE) " +
           "ORDER BY day ASC", nativeQuery = true)
    List<Object[]> getDailyRevenueSinceRaw(LocalDateTime since);
}
