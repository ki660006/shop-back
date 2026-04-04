package com.shop;

import com.shop.domain.catalog.entity.Category;
import com.shop.domain.catalog.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CategoryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        categoryRepository.deleteAll();
    }

    @Test
    void getCategoryTree_ReturnsHierarchicalStructure() throws Exception {
        // Given
        Category electronics = Category.builder()
                .name("Electronics")
                .slug("electronics")
                .build();
        electronics = categoryRepository.save(electronics);

        Category laptops = Category.builder()
                .name("Laptops")
                .slug("laptops")
                .parent(electronics)
                .build();
        laptops = categoryRepository.save(laptops);

        Category smartphones = Category.builder()
                .name("Smartphones")
                .slug("smartphones")
                .parent(electronics)
                .build();
        categoryRepository.save(smartphones);

        Category macbooks = Category.builder()
                .name("MacBooks")
                .slug("macbooks")
                .parent(laptops)
                .build();
        categoryRepository.save(macbooks);

        Category clothing = Category.builder()
                .name("Clothing")
                .slug("clothing")
                .build();
        categoryRepository.save(clothing);

        // When & Then
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[?(@.slug == 'electronics')].children", hasSize(2)))
                // Note: The order in children might not be guaranteed, but for small sets it usually is. 
                // Using more flexible jsonPath if needed.
                .andExpect(jsonPath("$[?(@.slug == 'electronics')].children[?(@.slug == 'laptops')].children", hasSize(1)))
                .andExpect(jsonPath("$[?(@.slug == 'clothing')].children", hasSize(0)));
    }
}
