package com.shop.domain.order.dto;

import com.shop.domain.payment.entity.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OrderCreateRequest(
        @NotBlank(message = "Shipping address is required")
        String shippingAddress,
        @NotBlank(message = "Receiver name is required")
        String receiverName,
        @NotBlank(message = "Receiver phone is required")
        String receiverPhone,
        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod
) {}
