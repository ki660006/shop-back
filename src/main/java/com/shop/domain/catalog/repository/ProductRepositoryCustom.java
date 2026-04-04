package com.shop.domain.catalog.repository;

import org.springframework.data.domain.Slice;
import com.shop.domain.catalog.entity.Product;
import com.shop.domain.catalog.dto.CursorRequest;
import com.shop.domain.catalog.dto.ProductSearchCondition;

public interface ProductRepositoryCustom {
    Slice<Product> searchProducts(ProductSearchCondition condition, CursorRequest cursorRequest);
}
