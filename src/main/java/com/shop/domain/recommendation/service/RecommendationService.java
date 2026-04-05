package com.shop.domain.recommendation.service;

import com.shop.domain.catalog.dto.ProductResponse;
import com.shop.domain.catalog.entity.Product;
import com.shop.domain.catalog.repository.ProductRepository;
import com.shop.domain.catalog.service.ProductService;
import com.shop.domain.recommendation.entity.RecentView;
import com.shop.domain.recommendation.repository.RecentViewRepository;
import com.shop.domain.user.entity.User;
import com.shop.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final RecentViewRepository recentViewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductService productService;

    @Transactional
    public void recordView(Long userId, UUID productId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        RecentView view = RecentView.builder()
                .user(user)
                .product(product)
                .build();

        recentViewRepository.save(view);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getRecentViews(Long userId, int limit) {
        return recentViewRepository.findRecentlyViewedProducts(userId, limit).stream()
                .map(productService::convertToProductResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getRecommendations(Long userId, int limit) {
        List<UUID> recentCategoryIds = recentViewRepository.findRecentlyViewedCategories(userId, 5);
        List<UUID> orderedCategoryIds = recentViewRepository.findRecentlyOrderedCategories(userId, 5);

        List<UUID> combinedCategoryIds = new ArrayList<>();
        if (recentCategoryIds != null) combinedCategoryIds.addAll(recentCategoryIds);
        if (orderedCategoryIds != null) combinedCategoryIds.addAll(orderedCategoryIds);
        
        List<UUID> uniqueCategoryIds = combinedCategoryIds.stream().distinct().collect(Collectors.toList());

        List<Product> recommendations = new ArrayList<>();

        if (!uniqueCategoryIds.isEmpty()) {
            recommendations.addAll(recentViewRepository.findTopSellingProductsInCategories(uniqueCategoryIds, limit));
        }

        if (recommendations.size() < limit) {
            List<Product> globalBestsellers = recentViewRepository.findGlobalBestsellers(limit - recommendations.size());
            for (Product p : globalBestsellers) {
                if (recommendations.stream().noneMatch(r -> r.getId().equals(p.getId()))) {
                    recommendations.add(p);
                }
            }
        }

        return recommendations.stream()
                .map(productService::convertToProductResponse)
                .collect(Collectors.toList());
    }

    private void aweciaweijfe() {
        
    }


    private void oookrokorf() {}
}
