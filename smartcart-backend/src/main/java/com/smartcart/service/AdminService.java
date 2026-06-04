package com.smartcart.service;

import com.smartcart.dto.DashboardDto;
import com.smartcart.dto.OrderDto;
import com.smartcart.dto.PagedResponse;
import com.smartcart.dto.UserDto;
import com.smartcart.entity.Order;
import com.smartcart.entity.User;
import com.smartcart.enums.OrderStatus;
import com.smartcart.enums.Role;
import com.smartcart.exception.ResourceNotFoundException;
import com.smartcart.repository.OrderRepository;
import com.smartcart.repository.ProductRepository;
import com.smartcart.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AdminService provides business logic for the administrative dashboard,
 * including real-time analytics, user role updates, and system-wide statistics.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public DashboardDto getDashboardStats() {
        log.info("Generating dashboard statistics");
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        List<Order> recentOrders = orderRepository.findAll(
                PageRequest.of(0, 5, Sort.by("createdAt").descending())
        ).getContent();

        List<OrderDto> recentOrderDtos = recentOrders.stream()
                .map(this::mapToOrderDto)
                .collect(Collectors.toList());

        DashboardDto stats = DashboardDto.builder()
                .totalUsers(userRepository.countByRole(Role.USER))
                .totalProducts(productRepository.countByActiveTrue())
                .totalOrders(orderRepository.count())
                .totalRevenue(orderRepository.getTotalRevenue())
                .pendingOrders(orderRepository.countByStatus(OrderStatus.PENDING))
                .deliveredOrders(orderRepository.countByStatus(OrderStatus.DELIVERED))
                .monthlyRevenue(orderRepository.getRevenueSince(thirtyDaysAgo))
                .monthlyOrders(orderRepository.countOrdersSince(thirtyDaysAgo))
                .recentOrders(recentOrderDtos)
                .dailyRevenue(orderRepository.getDailyRevenueSince(LocalDateTime.now().minusDays(7)))
                .build();

        log.info("Dashboard stats generated successfully");
        return stats;
    }

    public PagedResponse<UserDto> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> users = userRepository.findAll(pageable);

        List<UserDto> content = users.getContent().stream()
                .map(this::mapToUserDto)
                .collect(Collectors.toList());

        return PagedResponse.<UserDto>builder()
                .content(content)
                .page(users.getNumber())
                .size(users.getSize())
                .totalElements(users.getTotalElements())
                .totalPages(users.getTotalPages())
                .last(users.isLast())
                .build();
    }

    @Transactional
    public UserDto updateUserRole(Long userId, Role role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        user.setRole(role);
        user = userRepository.save(user);
        log.info("User {} role updated to {}", user.getEmail(), role);
        return mapToUserDto(user);
    }

    private UserDto mapToUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }

    private OrderDto mapToOrderDto(Order order) {
        return OrderDto.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .userName(order.getUser().getFirstName() + " " + order.getUser().getLastName())
                .userEmail(order.getUser().getEmail())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
