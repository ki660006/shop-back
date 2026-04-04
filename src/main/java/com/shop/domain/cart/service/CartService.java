package com.shop.domain.cart.service;

import com.shop.domain.cart.dto.CartResponse;
import java.util.UUID;
import com.shop.domain.cart.entity.Cart;
import com.shop.domain.cart.dto.CartIdentifier;

public interface CartService {
    CartResponse getCart(CartIdentifier identifier);
    void addItem(CartIdentifier identifier, UUID productId, int quantity);
    void updateQuantity(CartIdentifier identifier, UUID itemId, int quantity);
    void removeItem(CartIdentifier identifier, UUID itemId);
    void mergeCart(Long userId, UUID guestCartId);
    void clearCart(CartIdentifier identifier);
}
