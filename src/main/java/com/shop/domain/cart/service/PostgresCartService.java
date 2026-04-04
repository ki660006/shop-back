package com.shop.domain.cart.service;

import com.shop.domain.cart.dto.CartItemResponse;
import com.shop.domain.cart.dto.CartResponse;
import com.shop.domain.catalog.entity.Product;
import com.shop.domain.catalog.entity.ProductImage;
import com.shop.domain.catalog.repository.ProductRepository;
import com.shop.domain.user.entity.User;
import com.shop.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import com.shop.domain.cart.entity.CartItem;
import com.shop.domain.cart.repository.CartItemRepository;
import com.shop.domain.cart.entity.Cart;
import com.shop.domain.cart.dto.CartIdentifier;
import com.shop.domain.cart.repository.CartRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class PostgresCartService implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    private static final int MAX_ITEMS_PER_CART = 50;
    private static final int MAX_QUANTITY_PER_ITEM = 99;

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(CartIdentifier identifier) {
        Cart cart = findCartByIdentifier(identifier).orElse(null);
        if (cart == null) {
            return new CartResponse(List.of(), BigDecimal.ZERO);
        }

        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(this::mapToItemResponse)
                .collect(Collectors.toList());

        BigDecimal subtotal = itemResponses.stream()
                .map(item -> item.price().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(itemResponses, subtotal);
    }

    @Override
    public void addItem(CartIdentifier identifier, UUID productId, int quantity) {
        Cart cart = getOrCreateCart(identifier);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);

        int newQuantity = quantity;
        if (existingItem != null) {
            newQuantity += existingItem.getQuantity();
        } else {
            if (cart.getItems().size() >= MAX_ITEMS_PER_CART) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cart cannot have more than " + MAX_ITEMS_PER_CART + " unique items");
            }
        }

        validateQuantityAndStock(product, newQuantity);

        if (existingItem != null) {
            existingItem.setQuantity(newQuantity);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(newQuantity)
                    .build();
            cart.addItem(newItem);
        }
        
        cartRepository.save(cart);
    }

    @Override
    public void updateQuantity(CartIdentifier identifier, UUID itemId, int quantity) {
        Cart cart = findCartByIdentifier(identifier)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found"));

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart item not found"));

        validateQuantityAndStock(item.getProduct(), quantity);

        item.setQuantity(quantity);
        cartRepository.save(cart);
    }

    @Override
    public void removeItem(CartIdentifier identifier, UUID itemId) {
        Cart cart = findCartByIdentifier(identifier)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found"));

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart item not found"));

        cart.removeItem(item);
        cartRepository.save(cart);
    }

    @Override
    public void mergeCart(Long userId, UUID guestCartId) {
        Cart guestCart = cartRepository.findByGuestCartIdWithItems(guestCartId).orElse(null);
        if (guestCart == null || guestCart.getItems().isEmpty()) {
            if (guestCart != null) {
                cartRepository.delete(guestCart);
            }
            return;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Cart userCart = cartRepository.findByUserWithItems(user).orElseGet(() -> {
            Cart cart = new Cart();
            cart.setUser(user);
            return cartRepository.save(cart);
        });

        for (CartItem guestItem : guestCart.getItems()) {
            CartItem existingUserItem = userCart.getItems().stream()
                    .filter(item -> item.getProduct().getId().equals(guestItem.getProduct().getId()))
                    .findFirst()
                    .orElse(null);

            if (existingUserItem != null) {
                int newQuantity = Math.min(MAX_QUANTITY_PER_ITEM, existingUserItem.getQuantity() + guestItem.getQuantity());
                existingUserItem.setQuantity(newQuantity);
            } else if (userCart.getItems().size() < MAX_ITEMS_PER_CART) {
                CartItem newItem = CartItem.builder()
                        .cart(userCart)
                        .product(guestItem.getProduct())
                        .quantity(Math.min(MAX_QUANTITY_PER_ITEM, guestItem.getQuantity()))
                        .build();
                userCart.addItem(newItem);
            }
        }

        cartRepository.save(userCart);
        cartRepository.delete(guestCart);
    }

    @Override
    public void clearCart(CartIdentifier identifier) {
        Cart cart = findCartByIdentifier(identifier).orElse(null);
        if (cart != null) {
            cart.getItems().clear();
            cartRepository.save(cart);
        }
    }

    private Optional<Cart> findCartByIdentifier(CartIdentifier identifier) {
        if (identifier.isMember()) {
            User user = userRepository.findById(identifier.userId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
            return cartRepository.findByUserWithItems(user);
        } else if (identifier.isGuest()) {
            return cartRepository.findByGuestCartIdWithItems(identifier.guestCartId());
        }
        return Optional.empty();
    }

    private Cart getOrCreateCart(CartIdentifier identifier) {
        return findCartByIdentifier(identifier).orElseGet(() -> {
            Cart cart = new Cart();
            if (identifier.isMember()) {
                User user = userRepository.findById(identifier.userId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
                cart.setUser(user);
            } else {
                cart.setGuestCartId(identifier.guestCartId());
            }
            return cartRepository.save(cart);
        });
    }

    private void validateQuantityAndStock(Product product, int quantity) {
        if (quantity > MAX_QUANTITY_PER_ITEM) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity exceeds maximum limit of " + MAX_QUANTITY_PER_ITEM);
        }
        if (product.getStockQuantity() < quantity) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient stock for " + product.getName());
        }
    }

    private CartItemResponse mapToItemResponse(CartItem item) {
        Product product = item.getProduct();
        String imageUrl = product.getImages().stream()
                .filter(ProductImage::isPrimary)
                .findFirst()
                .map(ProductImage::getImageUrl)
                .orElse(product.getImages().isEmpty() ? null : product.getImages().get(0).getImageUrl());

        return new CartItemResponse(
                item.getId(),
                product.getId(),
                product.getName(),
                product.getPrice(),
                item.getQuantity(),
                imageUrl
        );
    }
}
