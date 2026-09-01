package com.smartcart.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.smartcart.entity.Payment;
import com.smartcart.enums.PaymentStatus;
import com.smartcart.exception.BadRequestException;
import com.smartcart.repository.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.util.Formatter;

@Slf4j
@Service
public class PaymentService {

    private final RazorpayClient razorpayClient;
    private final PaymentRepository paymentRepository;
    private final String keySecret;

    public PaymentService(RazorpayClient razorpayClient,
                          PaymentRepository paymentRepository,
                          @Value("${razorpay.key.secret}") String keySecret) {
        this.razorpayClient = razorpayClient;
        this.paymentRepository = paymentRepository;
        this.keySecret = keySecret;
    }

    // creates a razorpay order - returns the order ID for frontend
    public String createRazorpayOrder(BigDecimal amount, String receipt) {
        if ("yoursecrethere".equals(keySecret) || "placeholder_secret".equals(keySecret)) {
            String mockId = "mock_order_" + java.util.UUID.randomUUID().toString().substring(0, 8);
            log.info("Development Mode: Returning mock Razorpay order ID: {}", mockId);
            return mockId;
        }
        try {
            JSONObject orderRequest = new JSONObject();
            // Razorpay expects amount in paise (smallest currency unit)
            orderRequest.put("amount", amount.multiply(BigDecimal.valueOf(100)).intValue());
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", receipt);

            Order razorpayOrder = razorpayClient.orders.create(orderRequest);
            String razorpayOrderId = razorpayOrder.get("id");
            log.info("Razorpay order created: {} for receipt: {}", razorpayOrderId, receipt);
            return razorpayOrderId;
        } catch (RazorpayException e) {
            log.error("Failed to create Razorpay order: {}", e.getMessage());
            throw new BadRequestException("Payment gateway error. Please try again.");
        }
    }

    // HMAC-SHA256 signature check — ref: https://razorpay.com/docs/payments/server-integration/java/payment-gateway/build-integration/#14-verify-payment-signature
    public boolean verifyPaymentSignature(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) {
        if ("mock_signature".equals(razorpaySignature) && ("yoursecrethere".equals(keySecret) || "placeholder_secret".equals(keySecret))) {
            log.info("Development Mode: Validating mock signature");
            return true;
        }
        try {
            String payload = razorpayOrderId + "|" + razorpayPaymentId;
            String expectedSignature = calculateHmacSha256(payload, keySecret);
            return expectedSignature.equals(razorpaySignature);
        } catch (Exception e) {
            log.error("Signature verification failed: {}", e.getMessage());
            return false;
        }
    }

    @Transactional
    public void markPaymentCompleted(Payment payment, String razorpayPaymentId) {
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setTransactionId(razorpayPaymentId);
        paymentRepository.save(payment);
        log.info("Payment completed: {}", razorpayPaymentId);
    }

    @Transactional
    public void markPaymentFailed(Payment payment) {
        payment.setStatus(PaymentStatus.FAILED);
        paymentRepository.save(payment);
        log.warn("Payment failed for order: {}", payment.getOrder().getOrderNumber());
    }

    private String calculateHmacSha256(String data, String secret) throws Exception {
        SecretKeySpec signingKey = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(signingKey);
        byte[] rawHmac = mac.doFinal(data.getBytes());
        return toHexString(rawHmac);
    }

    private String toHexString(byte[] bytes) {
        Formatter formatter = new Formatter();
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }

    // live refund via razorpay API
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public String refundPayment(String transactionId, BigDecimal amount) throws RazorpayException {
        // Convert amount back to paise (assuming BigDecimal for precision)
        int amountInPaise = amount.multiply(BigDecimal.valueOf(100)).intValue();
        
        JSONObject refundRequest = new JSONObject();
        refundRequest.put("amount", amountInPaise);
        refundRequest.put("speed", "optimum");

        com.razorpay.Refund refund = razorpayClient.payments.refund(transactionId, refundRequest);
        log.info("Refund successfully created: {}", String.valueOf(refund.get("id")));
        return refund.get("id");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updatePaymentRefundStatus(Long paymentId, String refundId, com.smartcart.enums.RefundStatus status) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BadRequestException("Payment not found for ID: " + paymentId));
        payment.setRefundId(refundId);
        payment.setRefundStatus(status);
        paymentRepository.save(payment);
    }
}
