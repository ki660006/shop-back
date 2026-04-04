package com.shop.domain.catalog.dto;

import com.shop.domain.catalog.entity.ProductStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDetailResponse {
    private UUID id;
    private UUID categoryId;
    private String categoryName;
    private String name;
    private String description;
    private BigDecimal price;
    private ProductStatus status;
    private int stockQuantity;
    private OffsetDateTime createdAt;
    private List<ImageResponse> images;
    private List<ReviewResponse> reviews;
    private List<QaResponse> qas;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ImageResponse {
        private UUID id;
        private String imageUrl;
        private boolean isPrimary;
        private int sortOrder;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReviewResponse {
        private UUID id;
        private String author;
        private int rating;
        private String content;
        private OffsetDateTime createdAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QaResponse {
        private UUID id;
        private String author;
        private String question;
        private String answer;
        private OffsetDateTime createdAt;
    }
}
