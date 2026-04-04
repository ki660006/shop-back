package com.shop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.domain.catalog.dto.CategoryResponse;
import com.shop.domain.catalog.dto.CursorResponse;
import com.shop.domain.catalog.dto.ProductResponse;
import com.shop.domain.catalog.dto.ProductSearchCondition;
import com.shop.domain.catalog.entity.Category;
import com.shop.domain.catalog.entity.Product;
import com.shop.domain.catalog.entity.ProductStatus;
import com.shop.domain.catalog.repository.CategoryRepository;
import com.shop.domain.catalog.repository.ProductRepository;
import com.github.f4b6a3.uuid.UuidCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.shop.domain.catalog.dto.CategoryResponse;
import com.shop.domain.catalog.dto.ProductSearchCondition;
import com.shop.domain.catalog.dto.ProductResponse;

@SpringBootTest
@AutoConfigureMockMvc
public class CatalogIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    void productCatalogFlow() throws Exception {
        // 1. Create Categories
        Category electronics = categoryRepository.save(Category.builder()
                .id(UuidCreator.getTimeOrderedEpoch())
                .name("Electronics")
                .slug("electronics")
                .build());

        Category smartphones = categoryRepository.save(Category.builder()
                .id(UuidCreator.getTimeOrderedEpoch())
                .name("Smartphones")
                .slug("smartphones")
                .parent(electronics)
                .build());

        // 2. Create Products
        for (int i = 1; i <= 5; i++) {
            productRepository.save(Product.builder()
                    .id(UuidCreator.getTimeOrderedEpoch())
                    .name("Smartphone " + i)
                    .description("Latest smartphone model " + i)
                    .price(new BigDecimal(100 * i))
                    .status(ProductStatus.ON_SALE)
                    .stockQuantity(100)
                    .category(smartphones)
                    .build());
        }

        // 3. Test Category Tree
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Electronics")))
                .andExpect(jsonPath("$[0].children", hasSize(1)))
                .andExpect(jsonPath("$[0].children[0].name", is("Smartphones")));

        // 4. Test Product Search (FTS & Cursor Paging)
        // Note: FTS requires actual DB triggers/indices which might not be fully functional in H2,
        // but this test verifies the Querydsl execution logic.
        mockMvc.perform(get("/api/products")
                .param("size", "2")
                .param("sort", "PRICE_ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.hasNext", is(true)))
                .andExpect(jsonPath("$.nextCursor").exists());

        // 5. Test Product Detail
        Product firstProduct = productRepository.findAll().get(0);
        mockMvc.perform(get("/api/products/" + firstProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", startsWith("Smartphone")))
                .andExpect(jsonPath("$.reviews", notNullValue()))
                .andExpect(jsonPath("$.qas", notNullValue()));
    }
}
