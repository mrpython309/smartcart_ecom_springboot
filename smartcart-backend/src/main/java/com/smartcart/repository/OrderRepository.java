package com.smartcart.repository;

import com.smartcart.dto.DailyRevenueDto;
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

    @Query("SELECT new com.smartcart.dto.DailyRevenueDto(CAST(FUNCTION('DATE_FORMAT', o.createdAt, '%Y-%m-%d') AS String), SUM(o.totalAmount)) " +
           "FROM Order o WHERE o.createdAt >= :since AND o.status != 'CANCELLED' " +
           "GROUP BY FUNCTION('DATE_FORMAT', o.createdAt, '%Y-%m-%d') " +
           "ORDER BY FUNCTION('DATE_FORMAT', o.createdAt, '%Y-%m-%d') ASC")
    List<DailyRevenueDto> getDailyRevenueSince(LocalDateTime since);
}
