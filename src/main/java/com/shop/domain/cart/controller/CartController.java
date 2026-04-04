package com.shop.domain.cart.controller;

import com.shop.domain.cart.dto.CartMergeRequest;
import com.shop.domain.cart.dto.CartRequest;
import com.shop.domain.cart.dto.CartResponse;
import com.shop.domain.cart.dto.UpdateQuantityRequest;
import com.shop.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;
import com.shop.domain.cart.service.CartService;
import com.shop.domain.user.entity.User;
import com.shop.domain.cart.entity.Cart;
import com.shop.domain.cart.dto.CartIdentifier;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public CartResponse getCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "X-Guest-Cart-Id", required = false) UUID guestCartId) {
        CartIdentifier identifier = getIdentifier(userDetails, guestCartId);
        return cartService.getCart(identifier);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void addItem(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "X-Guest-Cart-Id", required = false) UUID guestCartId,
            @RequestBody @Valid CartRequest request) {
        CartIdentifier identifier = getIdentifier(userDetails, guestCartId);
        cartService.addItem(identifier, request.productId(), request.quantity());
    }

    @PostMapping("/merge")
    public void mergeCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid CartMergeRequest request) {
        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User must be authenticated to merge cart");
        }
        cartService.mergeCart(userDetails.getId(), request.guestCartId());
    }

    @PutMapping("/items/{itemId}")
    public void updateQuantity(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "X-Guest-Cart-Id", required = false) UUID guestCartId,
            @PathVariable UUID itemId,
            @RequestBody @Valid UpdateQuantityRequest request) {
        CartIdentifier identifier = getIdentifier(userDetails, guestCartId);
        cartService.updateQuantity(identifier, itemId, request.quantity());
    }

    @DeleteMapping("/items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeItem(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "X-Guest-Cart-Id", required = false) UUID guestCartId,
            @PathVariable UUID itemId) {
        CartIdentifier identifier = getIdentifier(userDetails, guestCartId);
        cartService.removeItem(identifier, itemId);
    }

    private CartIdentifier getIdentifier(CustomUserDetails userDetails, UUID guestCartId) {
        if (userDetails != null) {
            return CartIdentifier.forMember(userDetails.getId());
        }
        if (guestCartId != null) {
            return CartIdentifier.forGuest(guestCartId);
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No cart identifier provided (Member or Guest ID required)");
    }
}
