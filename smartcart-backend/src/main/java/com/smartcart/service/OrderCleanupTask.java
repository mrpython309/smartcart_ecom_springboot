package com.smartcart.service;

import com.smartcart.entity.Order;
import com.smartcart.entity.OrderItem;
import com.smartcart.entity.Product;
import com.smartcart.enums.OrderStatus;
import com.smartcart.repository.OrderRepository;
import com.smartcart.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled task to cleanup stale pending orders.
 * This prevents stock from being "leaked" when users abandon checkout.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCleanupTask {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    /**
     * Runs every 10 minutes.
     * Cancels orders stuck in PENDING for more than 30 minutes.
     */
    @Scheduled(fixedRate = 600000)
    @Transactional
    public void cleanupStaleOrders() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(30);
        
        // Custom query to find pending orders older than threshold
        // We'll use a direct JPQL query in the repository or just find all and filter
        // For efficiency, we filter in the database.
        List<Order> staleOrders = orderRepository.findPendingOrdersBefore(threshold);

        if (staleOrders.isEmpty()) {
            return;
        }

        log.info("Found {} stale pending orders. Starting cleanup...", staleOrders.size());

        for (Order order : staleOrders) {
            try {
                cancelAndRestoreStock(order);
                log.info("Stale order {} cancelled. Stock restored.", order.getOrderNumber());
            } catch (Exception e) {
                log.error("Failed to cleanup stale order {}: {}", order.getOrderNumber(), e.getMessage());
            }
        }
    }

    private void cancelAndRestoreStock(Order order) {
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());

        for (OrderItem item : order.getItems()) {
            if (item.getProduct() != null) {
                Product product = item.getProduct();
                product.setStock(product.getStock() + item.getQuantity());
                productRepository.save(product);
            }
        }
        orderRepository.save(order);
    }
}
