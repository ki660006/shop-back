package com.shop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.f4b6a3.uuid.UuidCreator;
import com.shop.domain.auth.dto.AuthResponse;
import com.shop.domain.auth.dto.LoginRequest;
import com.shop.domain.auth.dto.SignupRequest;
import com.shop.domain.cart.dto.CartRequest;
import com.shop.domain.cart.repository.CartItemRepository;
import com.shop.domain.cart.repository.CartRepository;
import com.shop.domain.catalog.entity.Category;
import com.shop.domain.catalog.entity.Product;
import com.shop.domain.catalog.entity.ProductStatus;
import com.shop.domain.catalog.repository.CategoryRepository;
import com.shop.domain.catalog.repository.ProductRepository;
import com.shop.domain.order.dto.OrderCreateRequest;
import com.shop.domain.order.dto.OrderResponse;
import com.shop.domain.order.repository.OrderItemRepository;
import com.shop.domain.order.repository.OrderRepository;
import com.shop.domain.payment.entity.PaymentMethod;
import com.shop.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class OrderIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    private UUID productId;
    private String accessToken;

    @BeforeEach
    void setUp() throws Exception {
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        // Setup Category and Product
        Category category = categoryRepository.save(Category.builder()
                .id(UuidCreator.getTimeOrderedEpoch())
                .name("Test Category")
                .slug("test-cat")
                .build());

        Product product = productRepository.save(Product.builder()
                .id(UuidCreator.getTimeOrderedEpoch())
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("10000"))
                .status(ProductStatus.ON_SALE)
                .stockQuantity(10)
                .category(category)
                .build());
        productId = product.getId();

        // Setup User and Token
        SignupRequest signupRequest = SignupRequest.builder()
                .email("order@example.com")
                .password("password123")
                .name("Order User")
                .nickname("orderer")
                .build();
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)));

        LoginRequest loginRequest = LoginRequest.builder()
                .email("order@example.com")
                .password("password123")
                .build();
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();
        
        AuthResponse authResponse = objectMapper.readValue(loginResult.getResponse().getContentAsString(), AuthResponse.class);
        accessToken = authResponse.getAccessToken();
    }

    @Test
    void checkoutOrderFlowAndHistoryTest() throws Exception {
        // 1. Add item to cart
        CartRequest cartRequest = new CartRequest(productId, 2);
        mockMvc.perform(post("/api/cart")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cartRequest)))
                .andExpect(status().isOk());

        // 2. Checkout
        OrderCreateRequest orderRequest = new OrderCreateRequest(
                "123 Test St",
                "Test Receiver",
                "010-1234-5678",
                PaymentMethod.CREDIT_CARD
        );

        MvcResult result = mockMvc.perform(post("/api/orders")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNumber", notNullValue()))
                .andExpect(jsonPath("$.status", is("COMPLETED")))
                .andReturn();

        OrderResponse orderResponse = objectMapper.readValue(result.getResponse().getContentAsString(), OrderResponse.class);
        UUID orderId = orderResponse.id();

        // 3. Verify stock is reduced (10 -> 8)
        Product product = productRepository.findById(productId).orElseThrow();
        assertEquals(8, product.getStockQuantity());

        // 4. Get order history
        mockMvc.perform(get("/api/orders")
                .header("Authorization", "Bearer " + accessToken)
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(orderId.toString())));

        // 5. Get order details
        mockMvc.perform(get("/api/orders/" + orderId)
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(orderId.toString())))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].quantity", is(2)));

        // 6. Cancel order
        mockMvc.perform(post("/api/orders/" + orderId + "/cancel")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        // 7. Verify stock is restored (8 -> 10)
        Product restoredProduct = productRepository.findById(productId).orElseThrow();
        assertEquals(10, restoredProduct.getStockQuantity());

        // 8. Verify order status is CANCELLED
        mockMvc.perform(get("/api/orders/" + orderId)
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")));
    }

    @Test
    void concurrentCheckoutTest() throws Exception {
        // Setup a product with 1 stock
        Category category = categoryRepository.findAll().get(0);
        Product limitedProduct = productRepository.save(Product.builder()
                .id(UuidCreator.getTimeOrderedEpoch())
                .name("Limited Product")
                .description("Limited")
                .price(new BigDecimal("1000"))
                .status(ProductStatus.ON_SALE)
                .stockQuantity(1)
                .category(category)
                .build());

        // Setup 2 users and tokens
        String[] tokens = new String[2];
        for (int i = 0; i < 2; i++) {
            String email = "concurrent" + i + "@example.com";
            mockMvc.perform(post("/api/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(SignupRequest.builder()
                            .email(email).password("pass").name("u" + i).nickname("n" + i).build())));
            
            MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(LoginRequest.builder().email(email).password("pass").build())))
                    .andReturn();
            tokens[i] = objectMapper.readValue(loginResult.getResponse().getContentAsString(), AuthResponse.class).getAccessToken();

            // Add the same product to their carts
            mockMvc.perform(post("/api/cart")
                    .header("Authorization", "Bearer " + tokens[i])
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new CartRequest(limitedProduct.getId(), 1))))
                    .andExpect(status().isOk());
        }

        // Try to checkout concurrently
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        OrderCreateRequest orderRequest = new OrderCreateRequest(
                "123 St", "Recv", "010", PaymentMethod.CREDIT_CARD);

        for (int i = 0; i < 2; i++) {
            final String token = tokens[i];
            executorService.submit(() -> {
                try {
                    MvcResult res = mockMvc.perform(post("/api/orders")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(orderRequest)))
                            .andReturn();
                    
                    if (res.getResponse().getStatus() == 200) {
                        successCount.incrementAndGet();
                    } else {
                        failCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // 1 user should succeed, 1 should fail because stock was 1
        assertEquals(1, successCount.get());
        assertEquals(1, failCount.get());

        Product afterProduct = productRepository.findById(limitedProduct.getId()).orElseThrow();
        assertEquals(0, afterProduct.getStockQuantity());
    }
}
