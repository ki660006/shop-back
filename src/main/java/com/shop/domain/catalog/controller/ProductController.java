package com.shop.domain.catalog.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import com.shop.domain.catalog.dto.ProductDetailResponse;
import com.shop.domain.catalog.dto.CursorRequest;
import com.shop.domain.catalog.dto.ProductSearchCondition;
import com.shop.domain.catalog.service.ProductService;
import com.shop.domain.catalog.dto.ProductResponse;
import com.shop.domain.catalog.dto.CursorResponse;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<CursorResponse<ProductResponse>> getProducts(
            ProductSearchCondition condition,
            CursorRequest cursorRequest) {
        return ResponseEntity.ok(productService.searchProducts(condition, cursorRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDetailResponse> getProductDetail(@PathVariable UUID id) {
        return ResponseEntity.ok(productService.getProductDetail(id));
    }
}
