package com.shop.domain.catalog.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import com.shop.domain.catalog.dto.CategoryResponse;
import com.shop.domain.catalog.repository.CategoryRepository;
import com.shop.domain.catalog.entity.Category;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @org.springframework.cache.annotation.Cacheable(value = "categoryTree", key = "'root'")
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoryTree() {
        List<Category> allCategories = categoryRepository.findAll();
        
        // Map all categories to CategoryResponse and store in a map for easy lookup
        Map<UUID, CategoryResponse> responseMap = allCategories.stream()
                .collect(Collectors.toMap(
                        Category::getId,
                        category -> CategoryResponse.builder()
                                .id(category.getId())
                                .name(category.getName())
                                .slug(category.getSlug())
                                .children(new ArrayList<>())
                                .build()
                ));

        List<CategoryResponse> rootCategories = new ArrayList<>();

        for (Category category : allCategories) {
            CategoryResponse response = responseMap.get(category.getId());
            if (category.getParent() == null) {
                rootCategories.add(response);
            } else {
                CategoryResponse parentResponse = responseMap.get(category.getParent().getId());
                if (parentResponse != null) {
                    parentResponse.getChildren().add(response);
                }
            }
        }

        return rootCategories;
    }
}
