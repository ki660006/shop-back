package com.shop.domain.catalog.dto;

import com.shop.domain.catalog.entity.ProductStatus;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSearchCondition {
    private UUID categoryId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private ProductStatus status;
    private String keyword;
    private SortOption sort;

    public enum SortOption {
        LATEST, PRICE_ASC, PRICE_DESC, NAME_ASC
    }

    public SortOption getSort() {
        return (sort == null) ? SortOption.LATEST : sort;
    }
}
