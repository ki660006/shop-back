package com.shop.domain.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import com.shop.domain.catalog.entity.Product;
import com.shop.domain.cart.entity.Cart;

public record CartRequest(
    @NotNull(message = "Product ID is required")
    UUID productId,
    
    @Min(value = 1, message = "Quantity must be at least 1")
    int quantity
) {
}
