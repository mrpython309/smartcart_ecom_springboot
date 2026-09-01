package com.smartcart.service;

import com.smartcart.dto.*;
import com.smartcart.entity.*;
import com.smartcart.enums.OrderStatus;
import com.smartcart.enums.PaymentMethod;
import com.smartcart.enums.PaymentStatus;
import com.smartcart.exception.BadRequestException;
import com.smartcart.exception.ResourceNotFoundException;
import com.smartcart.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final AddressRepository addressRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PaymentService paymentService;

    // creates order in PENDING state, reserves stock, clears cart
    // payment still needs to happen via razorpay after this
    @Transactional
    public OrderDto placeOrder(Long userId, CreateOrderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new BadRequestException("Cart is empty"));

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        Address address = addressRepository.findByIdAndUserId(request.getAddressId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", request.getAddressId()));

        // Validate stock and create order items
        BigDecimal totalAmount = BigDecimal.ZERO;

        Order order = Order.builder()
                .orderNumber("SC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .user(user)
                .status(OrderStatus.PENDING)
                .shippingAddress(address.getStreet())
                .shippingCity(address.getCity())
                .shippingState(address.getState())
                .shippingZipCode(address.getZipCode())
                .shippingCountry(address.getCountry())
                .totalAmount(BigDecimal.ZERO)
                .build();

        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();

            if (product.getStock() < cartItem.getQuantity()) {
                throw new BadRequestException("Insufficient stock for: " + product.getName());
            }

            // Reserve stock
            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .productName(product.getName())
                    .productImageUrl(product.getImageUrl())
                    .quantity(cartItem.getQuantity())
                    .unitPrice(cartItem.getUnitPrice())
                    .build();

            order.getItems().add(orderItem);
            totalAmount = totalAmount.add(cartItem.getSubtotal());
        }

        order.setTotalAmount(totalAmount);

        // Create PENDING payment record (will be completed after Razorpay verification)
        Payment payment = Payment.builder()
                .order(order)
                .amount(totalAmount)
                .method(request.getPaymentMethod() != null ? request.getPaymentMethod() : PaymentMethod.RAZORPAY)
                .status(PaymentStatus.PENDING)
                .build();

        order.setPayment(payment);
        order = orderRepository.save(order);

        // Clear cart
        cart.getItems().clear();
        cartRepository.save(cart);

        // TODO: send order confirmation email here
        log.info("Order placed (PENDING): {} for user: {}", order.getOrderNumber(), user.getEmail());
        return mapToOrderDto(order);
    }

    // marks order as confirmed after razorpay payment goes through
    @Transactional
    public OrderDto confirmOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        order.setStatus(OrderStatus.CONFIRMED);
        order = orderRepository.save(order);

        log.info("Order confirmed: {}", order.getOrderNumber());
        return mapToOrderDto(order);
    }

    // payment failed — cancel order and put stock back
    @Transactional
    public void handlePaymentFailure(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        order.setStatus(OrderStatus.CANCELLED);

        // restore stock
        // FIXME: this does N separate saves, should batch update
        for (OrderItem item : order.getItems()) {
            if (item.getProduct() != null) {
                Product product = item.getProduct();
                product.setStock(product.getStock() + item.getQuantity());
                productRepository.save(product);
            }
        }

        orderRepository.save(order);
        log.warn("Order cancelled due to payment failure: {}", order.getOrderNumber());
    }

    @Transactional(readOnly = true)
    public OrderDto getOrderById(Long userId, Long orderId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        return mapToOrderDto(order);
    }

    @Transactional(readOnly = true)
    public List<OrderDto> getUserOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToOrderDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PagedResponse<OrderDto> getAllOrders(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Order> orders = orderRepository.findAll(pageable);
        return mapToPagedResponse(orders);
    }

    @Transactional
    public OrderDto updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        order.setStatus(status);

        if (status == OrderStatus.CANCELLED && order.getPayment() != null) {
            order.getPayment().setStatus(PaymentStatus.REFUNDED);
            // Restore stock
            for (OrderItem item : order.getItems()) {
                if (item.getProduct() != null) {
                    Product product = item.getProduct();
                    product.setStock(product.getStock() + item.getQuantity());
                    productRepository.save(product);
                }
            }
        }

        order = orderRepository.save(order);
        log.info("Order {} status updated to {}", order.getOrderNumber(), status);
        return mapToOrderDto(order);
    }

    private OrderDto mapToOrderDto(Order order) {
        OrderDto.OrderDtoBuilder builder = OrderDto.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .shippingAddress(order.getShippingAddress())
                .shippingCity(order.getShippingCity())
                .shippingState(order.getShippingState())
                .shippingZipCode(order.getShippingZipCode())
                .shippingCountry(order.getShippingCountry())
                .items(order.getItems() != null ? order.getItems().stream().map(this::mapToOrderItemDto).collect(Collectors.toList()) : java.util.Collections.emptyList())
                .payment(order.getPayment() != null ? mapToPaymentDto(order.getPayment()) : null)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .cancelledAt(order.getCancelledAt());

        if (order.getUser() != null) {
            builder.userName(order.getUser().getFirstName() + " " + order.getUser().getLastName());
            builder.userEmail(order.getUser().getEmail());
        }

        if (order.getPayment() != null) {
            builder.refundId(order.getPayment().getRefundId());
            builder.refundStatus(order.getPayment().getRefundStatus() != null ? order.getPayment().getRefundStatus().name() : null);
        }

        return builder.build();
    }

    private OrderItemDto mapToOrderItemDto(OrderItem item) {
        return OrderItemDto.builder()
                .id(item.getId())
                .productId(item.getProduct() != null ? item.getProduct().getId() : null)
                .productName(item.getProductName())
                .productImageUrl(item.getProductImageUrl())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(item.getSubtotal())
                .build();
    }

    private PaymentDto mapToPaymentDto(Payment payment) {
        return PaymentDto.builder()
                .id(payment.getId())
                .amount(payment.getAmount())
                .method(payment.getMethod())
                .status(payment.getStatus())
                .transactionId(payment.getTransactionId())
                .createdAt(payment.getCreatedAt())
                .build();
    }

    private PagedResponse<OrderDto> mapToPagedResponse(Page<Order> page) {
        List<OrderDto> content = page.getContent().stream()
                .map(this::mapToOrderDto)
                .collect(Collectors.toList());
        return PagedResponse.<OrderDto>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    // user-initiated cancellation + refund
    @Transactional
    public OrderDto cancelUserOrder(Long userId, Long orderId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new BadRequestException("Order cannot be cancelled as it is already " + order.getStatus());
        }
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Order is already cancelled.");
        }

        // --- Automated Refund Process ---
        // We attempt a live refund but catch exceptions so the cancellation isn't blocked 
        // if the payment gateway is unreachable or balance is low.
        try {
            if (order.isPaid() && order.getPaymentId() != null) {
                log.info("Attempting automatic refund for order: {}", order.getOrderNumber());
                String refundId = paymentService.refundPayment(
                    order.getPaymentId(),
                    order.getTotalAmount()
                );
                paymentService.updatePaymentRefundStatus(order.getPayment().getId(), refundId, com.smartcart.enums.RefundStatus.SUCCESS);
                log.info("Refund successful for order: {}. Refund ID: {}", order.getOrderNumber(), refundId);
            }
        } catch (Exception e) {
            log.error("Automatic refund failed for order {}: {}. Manual intervention required.", 
                      order.getOrderNumber(), e.getMessage());
            if (order.getPayment() != null) {
                try {
                    paymentService.updatePaymentRefundStatus(order.getPayment().getId(), null, com.smartcart.enums.RefundStatus.FAILED);
                } catch (Exception inner) {
                    log.error("Failed to update refund status to FAILED for order {}: {}", 
                              order.getOrderNumber(), inner.getMessage());
                }
            }
        }
        // ---------------------------------

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(java.time.LocalDateTime.now());

        // Restore stock
        for (OrderItem item : order.getItems()) {
            if (item.getProduct() != null) {
                Product product = item.getProduct();
                product.setStock(product.getStock() + item.getQuantity());
                productRepository.save(product);
            }
        }

        order = orderRepository.save(order);
        log.info("Order {} cancelled by user {}", order.getOrderNumber(), userId);
        return mapToOrderDto(order);
    }
}
