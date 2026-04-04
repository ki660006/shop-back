package com.shop.domain.recommendation.repository;

import com.shop.domain.catalog.entity.Product;

import java.util.List;
import java.util.UUID;

public interface RecentViewRepositoryCustom {
    List<Product> findRecentlyViewedProducts(Long userId, int limit);
    List<UUID> findRecentlyViewedCategories(Long userId, int limit);
    List<UUID> findRecentlyOrderedCategories(Long userId, int limit);
    List<Product> findTopSellingProductsInCategories(List<UUID> categoryIds, int limit);
    List<Product> findGlobalBestsellers(int limit);
}
