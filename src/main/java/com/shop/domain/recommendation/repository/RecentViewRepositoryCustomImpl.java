package com.shop.domain.recommendation.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shop.domain.catalog.entity.Product;
import com.shop.domain.catalog.entity.QProduct;
import com.shop.domain.catalog.entity.QCategory;
import com.shop.domain.order.entity.QOrder;
import com.shop.domain.order.entity.QOrderItem;
import com.shop.domain.recommendation.entity.QRecentView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RecentViewRepositoryCustomImpl implements RecentViewRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Product> findRecentlyViewedProducts(Long userId, int limit) {
        QRecentView recentView = QRecentView.recentView;
        QProduct product = QProduct.product;

        return queryFactory
                .select(product)
                .from(recentView)
                .join(recentView.product, product)
                .where(recentView.user.id.eq(userId))
                .orderBy(recentView.viewedAt.desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public List<UUID> findRecentlyViewedCategories(Long userId, int limit) {
        QRecentView recentView = QRecentView.recentView;

        return queryFactory
                .select(recentView.product.category.id)
                .from(recentView)
                .where(recentView.user.id.eq(userId))
                .groupBy(recentView.product.category.id)
                .orderBy(recentView.viewedAt.max().desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public List<UUID> findRecentlyOrderedCategories(Long userId, int limit) {
        QOrder order = QOrder.order;
        QOrderItem orderItem = QOrderItem.orderItem;

        return queryFactory
                .select(orderItem.product.category.id)
                .from(orderItem)
                .join(orderItem.order, order)
                .where(order.user.id.eq(userId))
                .groupBy(orderItem.product.category.id)
                .orderBy(order.createdAt.max().desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public List<Product> findTopSellingProductsInCategories(List<UUID> categoryIds, int limit) {
        QProduct product = QProduct.product;
        QOrderItem orderItem = QOrderItem.orderItem;

        if (categoryIds == null || categoryIds.isEmpty()) {
            return List.of();
        }

        return queryFactory
                .select(product)
                .from(product)
                .leftJoin(orderItem).on(orderItem.product.eq(product))
                .where(product.category.id.in(categoryIds))
                .groupBy(product.id)
                .orderBy(orderItem.quantity.sum().desc().nullsLast())
                .limit(limit)
                .fetch();
    }

    @Override
    public List<Product> findGlobalBestsellers(int limit) {
        QProduct product = QProduct.product;
        QOrderItem orderItem = QOrderItem.orderItem;

        return queryFactory
                .select(product)
                .from(product)
                .leftJoin(orderItem).on(orderItem.product.eq(product))
                .groupBy(product.id)
                .orderBy(orderItem.quantity.sum().desc().nullsLast())
                .limit(limit)
                .fetch();
    }
}
