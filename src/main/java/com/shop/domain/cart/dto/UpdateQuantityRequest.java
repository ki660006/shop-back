package com.shop.domain.cart.dto;

import jakarta.validation.constraints.Min;
import com.shop.domain.cart.entity.Cart;

public record UpdateQuantityRequest(
    @Min(value = 1, message = "Quantity must be at least 1")
    int quantity
) {
}
