package com.shop.domain.cart.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import com.shop.domain.cart.entity.Cart;

public record CartMergeRequest(
    @NotNull(message = "Guest Cart ID is required")
    UUID guestCartId
) {
}
