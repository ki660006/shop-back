package com.shop.domain.cart.dto;

import java.math.BigDecimal;
import java.util.UUID;
import com.shop.domain.cart.entity.Cart;

public record CartItemResponse(
    UUID itemId,
    UUID productId,
    String name,
    BigDecimal price,
    int quantity,
    String imageUrl
) {
}
