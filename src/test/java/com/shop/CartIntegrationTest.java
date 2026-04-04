package com.shop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.domain.auth.dto.LoginRequest;
import com.shop.domain.auth.dto.SignupRequest;
import com.shop.domain.auth.dto.AuthResponse;
import com.shop.domain.cart.repository.CartRepository;
import com.shop.domain.cart.repository.CartItemRepository;
import com.shop.domain.cart.dto.CartRequest;
import com.shop.domain.cart.dto.CartMergeRequest;
import com.shop.domain.cart.dto.UpdateQuantityRequest;
import com.shop.domain.catalog.entity.Category;
import com.shop.domain.catalog.repository.CategoryRepository;
import com.shop.domain.catalog.entity.Product;
import com.shop.domain.catalog.repository.ProductRepository;
import com.shop.domain.catalog.entity.ProductStatus;
import com.shop.domain.user.repository.UserRepository;
import com.github.f4b6a3.uuid.UuidCreator;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.shop.domain.user.entity.User;
import com.shop.domain.cart.entity.Cart;

@SpringBootTest
@AutoConfigureMockMvc
public class CartIntegrationTest {

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

    private UUID productId;
    private String accessToken;

    @BeforeEach
    void setUp() throws Exception {
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
                .stockQuantity(100)
                .category(category)
                .build());
        productId = product.getId();

        // Setup User and Token
        SignupRequest signupRequest = SignupRequest.builder()
                .email("cart@example.com")
                .password("password123")
                .name("Cart User")
                .nickname("carter")
                .build();
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)));

        LoginRequest loginRequest = LoginRequest.builder()
                .email("cart@example.com")
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
    void guestCartFlow() throws Exception {
        UUID guestCartId = UUID.randomUUID();
        CartRequest cartRequest = new CartRequest(productId, 2);

        // Add to guest cart
        mockMvc.perform(post("/api/cart")
                .header("X-Guest-Cart-Id", guestCartId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cartRequest)))
                .andExpect(status().isOk());

        // View guest cart
        mockMvc.perform(get("/api/cart")
                .header("X-Guest-Cart-Id", guestCartId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].name", is("Test Product")))
                .andExpect(jsonPath("$.items[0].quantity", is(2)));
    }

    @Test
    void memberCartAndMergeFlow() throws Exception {
        UUID guestCartId = UUID.randomUUID();
        CartRequest guestRequest = new CartRequest(productId, 1);

        // 1. Add to guest cart
        mockMvc.perform(post("/api/cart")
                .header("X-Guest-Cart-Id", guestCartId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(guestRequest)))
                .andExpect(status().isOk());

        // 2. Merge to member cart
        CartMergeRequest mergeRequest = new CartMergeRequest(guestCartId);
        mockMvc.perform(post("/api/cart/merge")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mergeRequest)))
                .andExpect(status().isOk());

        // 3. Verify member cart contains merged item
        mockMvc.perform(get("/api/cart")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].quantity", is(1)));

        // 4. Update quantity
        UUID itemId = UUID.fromString(objectMapper.readTree(
                mockMvc.perform(get("/api/cart")
                        .header("Authorization", "Bearer " + accessToken))
                        .andReturn().getResponse().getContentAsString()
        ).get("items").get(0).get("id").asText());

        UpdateQuantityRequest updateRequest = new UpdateQuantityRequest(5);
        mockMvc.perform(put("/api/cart/items/" + itemId)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity", is(5)));

        // 5. Delete item
        mockMvc.perform(delete("/api/cart/items/" + itemId)
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/cart")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", is(empty())));
    }

    @Test
    void stockValidationTest() throws Exception {
        // Product has 100 stock. Try adding 101.
        CartRequest cartRequest = new CartRequest(productId, 101);

        mockMvc.perform(post("/api/cart")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cartRequest)))
                .andExpect(status().isBadRequest());
    }
}
