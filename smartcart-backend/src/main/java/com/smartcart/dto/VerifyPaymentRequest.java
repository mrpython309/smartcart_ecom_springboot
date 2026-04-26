package com.smartcart.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyPaymentRequest {
    @NotBlank(message = "Razorpay Order ID is required")
    private String razorpayOrderId;
    @NotBlank(message = "Razorpay Payment ID is required")
    private String razorpayPaymentId;
    @NotBlank(message = "Razorpay Signature is required")
    private String razorpaySignature;
    @NotNull(message = "Order ID is required")
    private Long orderId;
}
