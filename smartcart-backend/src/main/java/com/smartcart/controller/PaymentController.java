package com.smartcart.controller;

import com.smartcart.dto.*;
import com.smartcart.entity.Order;
import com.smartcart.entity.Payment;
import com.smartcart.entity.User;
import com.smartcart.enums.OrderStatus;
import com.smartcart.exception.BadRequestException;
import com.smartcart.exception.ResourceNotFoundException;
import com.smartcart.repository.OrderRepository;
import com.smartcart.service.OrderService;
import com.smartcart.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderService orderService;
    private final OrderRepository orderRepository;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    /**
     * Creates a Razorpay order for the given SmartCart order.
     * Called after order is placed with PENDING status.
     */
    @PostMapping("/create-order")
    public ResponseEntity<ApiResponse<PaymentOrderResponse>> createPaymentOrder(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreatePaymentRequest request) {

        Order order = orderRepository.findByIdAndUserId(request.getOrderId(), user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", request.getOrderId()));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("Order is not in PENDING state. Current: " + order.getStatus());
        }

        if (order.getTotalAmount().compareTo(java.math.BigDecimal.ZERO) == 0) {
            orderService.confirmOrder(order.getId());
            return ResponseEntity.ok(ApiResponse.success("Zero-cost order automatically confirmed.", null));
        }

        // Create Razorpay order
        String razorpayOrderId = paymentService.createRazorpayOrder(
                order.getTotalAmount(),
                order.getOrderNumber()
        );

        // Save Razorpay order ID on payment
        Payment payment = order.getPayment();
        if (payment != null) {
            payment.setRazorpayOrderId(razorpayOrderId);
            orderRepository.save(order);
        }

        PaymentOrderResponse response = PaymentOrderResponse.builder()
                .razorpayOrderId(razorpayOrderId)
                .amount(order.getTotalAmount())
                .currency("INR")
                .razorpayKeyId(razorpayKeyId)
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Verifies the Razorpay payment signature and confirms the order.
     */
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<OrderDto>> verifyPayment(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody VerifyPaymentRequest request) {

        Order order = orderRepository.findByIdAndUserId(request.getOrderId(), user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", request.getOrderId()));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("Order is not in PENDING state");
        }

        boolean isValid = paymentService.verifyPaymentSignature(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature()
        );

        if (!isValid) {
            // Mark payment as failed, restore stock
            if (order.getPayment() != null) {
                paymentService.markPaymentFailed(order.getPayment());
            }
            orderService.handlePaymentFailure(order.getId());
            throw new BadRequestException("Payment verification failed. Your order has been cancelled and stock restored.");
        }

        // Mark payment completed and confirm order
        if (order.getPayment() != null) {
            paymentService.markPaymentCompleted(order.getPayment(), request.getRazorpayPaymentId());
        }

        OrderDto orderDto = orderService.confirmOrder(order.getId());
        log.info("Payment verified and order confirmed: {}", order.getOrderNumber());

        return ResponseEntity.ok(ApiResponse.success("Payment successful! Order confirmed.", orderDto));
    }
}
