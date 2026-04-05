package com.shop.domain.order.service;

import com.shop.domain.cart.dto.CartIdentifier;
import com.shop.domain.cart.entity.Cart;
import com.shop.domain.cart.entity.CartItem;
import com.shop.domain.cart.repository.CartRepository;
import com.shop.domain.cart.service.CartService;
import com.shop.domain.catalog.entity.Product;
import com.shop.domain.catalog.repository.ProductRepository;
import com.shop.domain.order.dto.OrderCreateRequest;
import com.shop.domain.order.dto.OrderItemResponse;
import com.shop.domain.order.dto.OrderResponse;
import com.shop.domain.order.entity.Order;
import com.shop.domain.order.entity.OrderItem;
import com.shop.domain.order.entity.OrderStatus;
import com.shop.domain.order.repository.OrderRepository;
import com.shop.domain.payment.service.PaymentService;
import com.shop.domain.user.entity.User;
import com.shop.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderNumberGenerator orderNumberGenerator;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PaymentService paymentService;
    private final CartService cartService;

    @Transactional
    public OrderResponse createOrder(Long userId, OrderCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Cart cart = cartRepository.findByUserWithItems(user)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found"));

        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        String orderNumber = orderNumberGenerator.generate();
        Order order = Order.builder()
                .user(user)
                .orderNumber(orderNumber)
                .status(OrderStatus.PENDING)
                .shippingAddress(request.shippingAddress())
                .receiverName(request.receiverName())
                .receiverPhone(request.receiverPhone())
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            Product product = productRepository.findByIdWithPessimisticLock(cartItem.getProduct().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found"));

            product.decreaseStock(cartItem.getQuantity());

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .price(product.getPrice())
                    .build();

            order.addItem(orderItem);
            totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }

        order.setTotalAmount(totalAmount);
        
        Order savedOrder = orderRepository.save(order);

        paymentService.processPayment(savedOrder, request.paymentMethod());

        savedOrder.setStatus(OrderStatus.COMPLETED);

        cartService.clearCart(CartIdentifier.forMember(userId));

        List<OrderItemResponse> itemResponses = savedOrder.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getPrice()
                )).toList();

        return new OrderResponse(
                savedOrder.getId(),
                savedOrder.getOrderNumber(),
                savedOrder.getStatus(),
                savedOrder.getTotalAmount(),
                savedOrder.getShippingAddress(),
                savedOrder.getReceiverName(),
                savedOrder.getReceiverPhone(),
                savedOrder.getCreatedAt(),
                itemResponses
        );
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrders(Long userId, Pageable pageable) {
        Page<Order> orders = orderRepository.findByUserId(userId, pageable);
        return orders.map(this::mapToOrderResponse);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(UUID orderId, Long userId) {
        Order order = orderRepository.findWithItemsById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (!order.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Order does not belong to user");
        }

        return mapToOrderResponse(order);
    }

    @Transactional
    public void cancelOrder(UUID orderId, Long userId) {
        Order order = orderRepository.findWithItemsById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (!order.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Order does not belong to user");
        }

        if (order.getStatus() != OrderStatus.COMPLETED && order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Order cannot be cancelled in status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);

        for (OrderItem item : order.getItems()) {
            Product product = productRepository.findByIdWithPessimisticLock(item.getProduct().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found"));
            product.increaseStock(item.getQuantity());
        }
    }

    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getPrice()
                )).toList();

        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getShippingAddress(),
                order.getReceiverName(),
                order.getReceiverPhone(),
                order.getCreatedAt(),
                itemResponses
        );
    }

    private void test() {
        
    }

    private void acmaweijfpawjeifji() {}
}
