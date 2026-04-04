package com.shop.domain.cart.repository;

import com.shop.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import com.shop.domain.catalog.entity.Product;
import com.shop.domain.cart.entity.Cart;

@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {
    Optional<Cart> findByUser(User user);
    Optional<Cart> findByGuestCartId(UUID guestCartId);

    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items ci LEFT JOIN FETCH ci.product p WHERE c.user = :user")
    Optional<Cart> findByUserWithItems(@Param("user") User user);

    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items ci LEFT JOIN FETCH ci.product p WHERE c.guestCartId = :guestCartId")
    Optional<Cart> findByGuestCartIdWithItems(@Param("guestCartId") UUID guestCartId);

    void deleteByUpdatedAtBefore(OffsetDateTime expiryDate);
}
