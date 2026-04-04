package com.shop.domain.catalog.service;

import com.shop.global.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import com.shop.domain.catalog.repository.ProductRepository;
import com.shop.domain.catalog.entity.Product;
import com.shop.domain.catalog.dto.ProductDetailResponse;
import com.shop.domain.catalog.dto.CursorRequest;
import com.shop.domain.catalog.dto.ProductSearchCondition;
import com.shop.domain.catalog.entity.ProductImage;
import com.shop.domain.catalog.dto.ProductResponse;
import com.shop.domain.catalog.dto.CursorResponse;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public List<ProductDetailResponse.ImageResponse> uploadProductImages(UUID productId, List<org.springframework.web.multipart.MultipartFile> files) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        List<com.shop.domain.catalog.entity.ProductImage> newImages = new java.util.ArrayList<>();
        for (org.springframework.web.multipart.MultipartFile file : files) {
            String fileName = fileStorageService.storeFile(file);
            
            boolean isPrimary = product.getImages().isEmpty() && newImages.isEmpty();
            
            com.shop.domain.catalog.entity.ProductImage productImage = com.shop.domain.catalog.entity.ProductImage.builder()
                    .id(com.github.f4b6a3.uuid.UuidCreator.getTimeOrderedEpoch())
                    .product(product)
                    .imageUrl(fileName)
                    .isPrimary(isPrimary)
                    .sortOrder(product.getImages().size() + newImages.size())
                    .build();
            
            newImages.add(productImage);
        }

        product.getImages().addAll(newImages);
        productRepository.save(product);

        return newImages.stream()
                .map(img -> ProductDetailResponse.ImageResponse.builder()
                        .id(img.getId())
                        .imageUrl("/uploads/" + img.getImageUrl())
                        .isPrimary(img.isPrimary())
                        .sortOrder(img.getSortOrder())
                        .build())
                .collect(Collectors.toList());
    }

    public CursorResponse<ProductResponse> searchProducts(ProductSearchCondition condition, CursorRequest cursorRequest) {
        Slice<Product> productSlice = productRepository.searchProducts(condition, cursorRequest);
        
        List<ProductResponse> content = productSlice.getContent().stream()
                .map(this::convertToProductResponse)
                .collect(Collectors.toList());

        String nextCursor = null;
        if (productSlice.hasNext()) {
            nextCursor = encodeCursor(productSlice.getContent().get(productSlice.getContent().size() - 1), condition.getSort());
        }

        return CursorResponse.<ProductResponse>builder()
                .content(content)
                .nextCursor(nextCursor)
                .hasNext(productSlice.hasNext())
                .build();
    }

    public ProductDetailResponse getProductDetail(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));

        return ProductDetailResponse.builder()
                .id(product.getId())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .status(product.getStatus())
                .stockQuantity(product.getStockQuantity())
                .createdAt(product.getCreatedAt())
                .images(product.getImages().stream()
                        .map(img -> ProductDetailResponse.ImageResponse.builder()
                                .id(img.getId())
                                .imageUrl("/uploads/" + img.getImageUrl())
                                .isPrimary(img.isPrimary())
                                .sortOrder(img.getSortOrder())
                                .build())
                        .collect(Collectors.toList()))
                .reviews(getMockReviews())
                .qas(getMockQas())
                .build();
    }

    public ProductResponse convertToProductResponse(Product product) {
        String mainImageUrl = product.getImages().stream()
                .filter(com.shop.domain.catalog.entity.ProductImage::isPrimary)
                .findFirst()
                .map(img -> "/uploads/" + img.getImageUrl())
                .orElse(product.getImages().isEmpty() ? null : "/uploads/" + product.getImages().get(0).getImageUrl());

        return ProductResponse.builder()
                .id(product.getId())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .name(product.getName())
                .mainImageUrl(mainImageUrl)
                .price(product.getPrice())
                .status(product.getStatus())
                .stockQuantity(product.getStockQuantity())
                .createdAt(product.getCreatedAt())
                .build();
    }

    private String encodeCursor(Product lastProduct, ProductSearchCondition.SortOption sort) {
        String val = switch (sort) {
            case LATEST -> lastProduct.getCreatedAt().toString();
            case PRICE_ASC, PRICE_DESC -> lastProduct.getPrice().toString();
            case NAME_ASC -> lastProduct.getName();
        };
        String combined = val + "," + lastProduct.getId().toString();
        return Base64.getEncoder().encodeToString(combined.getBytes());
    }

    private List<ProductDetailResponse.ReviewResponse> getMockReviews() {
        return List.of(
                ProductDetailResponse.ReviewResponse.builder()
                        .id(UUID.randomUUID())
                        .author("John Doe")
                        .rating(5)
                        .content("Excellent product! Highly recommended.")
                        .createdAt(OffsetDateTime.now().minusDays(2))
                        .build(),
                ProductDetailResponse.ReviewResponse.builder()
                        .id(UUID.randomUUID())
                        .author("Jane Smith")
                        .rating(4)
                        .content("Good quality, but shipping was a bit slow.")
                        .createdAt(OffsetDateTime.now().minusDays(5))
                        .build()
        );
    }

    private List<ProductDetailResponse.QaResponse> getMockQas() {
        return List.of(
                ProductDetailResponse.QaResponse.builder()
                        .id(UUID.randomUUID())
                        .author("Curious Buyer")
                        .question("Is this item in stock?")
                        .answer("Yes, it is currently in stock and ready to ship.")
                        .createdAt(OffsetDateTime.now().minusDays(1))
                        .build()
        );
    }
}
