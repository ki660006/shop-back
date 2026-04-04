package com.shop.domain.order.dto;

import com.shop.domain.order.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        String orderNumber,
        OrderStatus status,
        BigDecimal totalAmount,
        String shippingAddress,
        String receiverName,
        String receiverPhone,
        OffsetDateTime createdAt,
        List<OrderItemResponse> items
) {}
