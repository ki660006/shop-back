package com.shop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.f4b6a3.uuid.UuidCreator;
import com.shop.domain.auth.dto.AuthResponse;
import com.shop.domain.auth.dto.LoginRequest;
import com.shop.domain.auth.dto.SignupRequest;
import com.shop.domain.auth.repository.RefreshTokenRepository;
import com.shop.domain.catalog.entity.Category;
import com.shop.domain.catalog.entity.Product;
import com.shop.domain.catalog.entity.ProductStatus;
import com.shop.domain.catalog.repository.CategoryRepository;
import com.shop.domain.catalog.repository.ProductRepository;
import com.shop.domain.recommendation.repository.RecentViewRepository;
import com.shop.domain.user.repository.UserRepository;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import jakarta.persistence.EntityManagerFactory;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class RecommendationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private RecentViewRepository recentViewRepository;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private String accessToken;
    private Product savedProduct;
    private Category savedCategory;

    @BeforeEach
    void setUp() throws Exception {
        recentViewRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();

        // Evict all caches to ensure a clean state for each test
        cacheManager.getCacheNames().forEach(name -> {
            Cache cache = cacheManager.getCache(name);
            if (cache != null) {
                cache.clear();
            }
        });

        // Create test category
        savedCategory = categoryRepository.save(Category.builder()
                .id(UuidCreator.getTimeOrderedEpoch())
                .name("Electronics")
                .slug("electronics")
                .build());

        // Create test product
        savedProduct = productRepository.save(Product.builder()
                .id(UuidCreator.getTimeOrderedEpoch())
                .name("Test Laptop")
                .description("A powerful test laptop")
                .price(new BigDecimal("1500000"))
                .status(ProductStatus.ON_SALE)
                .stockQuantity(50)
                .category(savedCategory)
                .build());

        // Sign up and log in a user
        SignupRequest signupRequest = SignupRequest.builder()
                .email("rec@example.com")
                .password("password123")
                .name("Rec User")
                .nickname("recuser")
                .build();
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)));

        LoginRequest loginRequest = LoginRequest.builder()
                .email("rec@example.com")
                .password("password123")
                .build();
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        AuthResponse authResponse = objectMapper.readValue(
                loginResult.getResponse().getContentAsString(), AuthResponse.class);
        accessToken = authResponse.getAccessToken();
    }

    @Test
    void testRecordView() throws Exception {
        // Post a product view for authenticated user
        mockMvc.perform(post("/api/products/" + savedProduct.getId() + "/view")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        // Verify the RecentView was persisted
        long viewCount = recentViewRepository.count();
        assertTrue(viewCount >= 1, "Expected at least one RecentView record after recording a view");

        // Verify recent views endpoint returns the product
        mockMvc.perform(get("/api/products/recent")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(savedProduct.getId().toString()));
    }

    @Test
    void testGetRecommendations() throws Exception {
        // Record a view to seed recommendation data
        mockMvc.perform(post("/api/products/" + savedProduct.getId() + "/view")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        // Verify recommendations returns a valid list for authenticated user
        mockMvc.perform(get("/api/recommendations")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // Verify unauthenticated user gets global bestsellers (empty or non-empty list)
        mockMvc.perform(get("/api/recommendations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testCategoryCache() throws Exception {
        // Enable Hibernate statistics so we can measure query counts
        Statistics stats = entityManagerFactory.unwrap(org.hibernate.SessionFactory.class).getStatistics();
        stats.setStatisticsEnabled(true);
        stats.clear();

        // First call: should execute a SQL query to fetch categories
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk());

        long queryCountAfterFirstCall = stats.getQueryExecutionCount() + stats.getEntityLoadCount();
        assertTrue(queryCountAfterFirstCall >= 1,
                "Expected at least one query on the first category tree fetch");

        // Reset stats to measure second call in isolation
        stats.clear();

        // Second call: should be served from cache with zero DB queries
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk());

        long queryCountAfterSecondCall = stats.getQueryExecutionCount() + stats.getEntityLoadCount();
        assertTrue(queryCountAfterSecondCall == 0,
                "Expected zero DB queries on the second category tree fetch (should be served from cache), but got: "
                        + queryCountAfterSecondCall);

        // Verify the cache is populated
        Cache categoryTreeCache = cacheManager.getCache("categoryTree");
        assertNotNull(categoryTreeCache, "categoryTree cache should exist");
        assertNotNull(categoryTreeCache.get("root"), "categoryTree cache should contain 'root' key after first call");
    }

    @Test
    void testSwaggerUiAccess() throws Exception {
        // Verify API docs endpoint is publicly accessible without authentication
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());

        // Verify Swagger UI HTML is accessible without authentication
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }
}
