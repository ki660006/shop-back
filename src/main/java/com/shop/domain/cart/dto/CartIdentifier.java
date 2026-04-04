package com.shop.domain.cart.dto;

import java.util.UUID;
import com.shop.domain.cart.entity.Cart;

/**
 * Unified identifier for both member and guest shopping carts.
 * Member carts are identified by userId, guest carts by guestCartId.
 */
public record CartIdentifier(
    Long userId,
    UUID guestCartId
) {
    public static CartIdentifier forMember(Long userId) {
        return new CartIdentifier(userId, null);
    }

    public static CartIdentifier forGuest(UUID guestCartId) {
        return new CartIdentifier(null, guestCartId);
    }

    public boolean isMember() {
        return userId != null;
    }

    public boolean isGuest() {
        return guestCartId != null;
    }
}
