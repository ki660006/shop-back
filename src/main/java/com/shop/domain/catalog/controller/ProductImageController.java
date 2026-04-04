package com.shop.domain.catalog.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import com.shop.domain.catalog.dto.ProductDetailResponse;
import com.shop.domain.catalog.service.ProductService;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductImageController {

    private final ProductService productService;

    @PostMapping("/{productId}/images")
    public ResponseEntity<List<ProductDetailResponse.ImageResponse>> uploadImages(
            @PathVariable UUID productId,
            @RequestParam("files") List<MultipartFile> files) {
        
        List<ProductDetailResponse.ImageResponse> responses = productService.uploadProductImages(productId, files);
        return ResponseEntity.ok(responses);
    }
}
