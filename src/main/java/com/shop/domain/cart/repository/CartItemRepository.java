package com.shop.domain.cart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import com.shop.domain.cart.entity.CartItem;
import com.shop.domain.cart.entity.Cart;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {
}
