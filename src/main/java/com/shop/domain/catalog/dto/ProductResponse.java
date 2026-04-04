package com.shop.domain.catalog.dto;

import com.shop.domain.catalog.entity.ProductStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {
    private UUID id;
    private UUID categoryId;
    private String categoryName;
    private String name;
    private String mainImageUrl;
    private BigDecimal price;
    private ProductStatus status;
    private int stockQuantity;
    private OffsetDateTime createdAt;
}
