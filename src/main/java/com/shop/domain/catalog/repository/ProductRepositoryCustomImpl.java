package com.shop.domain.catalog.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import com.shop.domain.catalog.entity.Product;
import com.shop.domain.catalog.dto.CursorRequest;
import com.shop.domain.catalog.entity.ProductStatus;
import com.shop.domain.catalog.dto.ProductSearchCondition;
import com.shop.domain.catalog.entity.Category;
import com.shop.domain.catalog.entity.QCategory;
import com.shop.domain.catalog.entity.QProduct;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryCustomImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<Product> searchProducts(ProductSearchCondition condition, CursorRequest cursorRequest) {
        QProduct product = QProduct.product;
        QCategory category = QCategory.category;

        List<Product> products = queryFactory
                .selectFrom(product)
                .leftJoin(product.category, category).fetchJoin()
                .where(
                        categoryEq(condition.getCategoryId()),
                        priceGoe(condition.getMinPrice()),
                        priceLoe(condition.getMaxPrice()),
                        statusEq(condition.getStatus()),
                        keywordContains(condition.getKeyword()),
                        cursorPredicate(condition.getSort(), cursorRequest.getCursor())
                )
                .orderBy(orderSpecifiers(condition.getSort()))
                .limit(cursorRequest.getSize() + 1)
                .fetch();

        boolean hasNext = products.size() > cursorRequest.getSize();
        if (hasNext) {
            products.remove(cursorRequest.getSize());
        }

        return new SliceImpl<>(products, cursorRequest.getSize() == 0 ? null : null, hasNext); // Pageable is not really used with cursors
    }

    private BooleanExpression categoryEq(UUID categoryId) {
        return categoryId != null ? QProduct.product.category.id.eq(categoryId) : null;
    }

    private BooleanExpression priceGoe(BigDecimal minPrice) {
        return minPrice != null ? QProduct.product.price.goe(minPrice) : null;
    }

    private BooleanExpression priceLoe(BigDecimal maxPrice) {
        return maxPrice != null ? QProduct.product.price.loe(maxPrice) : null;
    }

    private BooleanExpression statusEq(ProductStatus status) {
        return status != null ? QProduct.product.status.eq(status) : null;
    }

    private BooleanExpression keywordContains(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        // FTS implementation
        return Expressions.booleanTemplate("search_vector @@ plainto_tsquery('english', {0})", keyword);
    }

    private BooleanExpression cursorPredicate(ProductSearchCondition.SortOption sort, String cursor) {
        if (!StringUtils.hasText(cursor)) {
            return null;
        }

        String decoded = new String(Base64.getDecoder().decode(cursor));
        String[] parts = decoded.split(",");
        if (parts.length != 2) {
            return null;
        }

        QProduct product = QProduct.product;
        String val = parts[0];
        UUID lastId = UUID.fromString(parts[1]);

        return switch (sort) {
            case LATEST -> {
                OffsetDateTime lastCreatedAt = OffsetDateTime.parse(val);
                yield product.createdAt.lt(lastCreatedAt)
                        .or(product.createdAt.eq(lastCreatedAt).and(product.id.lt(lastId)));
            }
            case PRICE_ASC -> {
                BigDecimal lastPrice = new BigDecimal(val);
                yield product.price.gt(lastPrice)
                        .or(product.price.eq(lastPrice).and(product.id.gt(lastId)));
            }
            case PRICE_DESC -> {
                BigDecimal lastPrice = new BigDecimal(val);
                yield product.price.lt(lastPrice)
                        .or(product.price.eq(lastPrice).and(product.id.lt(lastId)));
            }
            case NAME_ASC -> product.name.gt(val)
                    .or(product.name.eq(val).and(product.id.gt(lastId)));
        };
    }

    private OrderSpecifier<?>[] orderSpecifiers(ProductSearchCondition.SortOption sort) {
        QProduct product = QProduct.product;
        return switch (sort) {
            case LATEST -> new OrderSpecifier[]{product.createdAt.desc(), product.id.desc()};
            case PRICE_ASC -> new OrderSpecifier[]{product.price.asc(), product.id.asc()};
            case PRICE_DESC -> new OrderSpecifier[]{product.price.desc(), product.id.desc()};
            case NAME_ASC -> new OrderSpecifier[]{product.name.asc(), product.id.asc()};
        };
    }
}
