package com.shop.domain.cart.dto;

import java.math.BigDecimal;
import java.util.List;
import com.shop.domain.cart.entity.Cart;

public record CartResponse(
    List<CartItemResponse> items,
    BigDecimal subtotal
) {
}
