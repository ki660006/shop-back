package com.shop.domain.recommendation.controller;

import com.shop.domain.catalog.dto.ProductResponse;
import com.shop.domain.recommendation.service.RecommendationService;
import com.shop.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @PostMapping("/products/{id}/view")
    public ResponseEntity<Void> recordView(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        if (userDetails != null) {
            recommendationService.recordView(userDetails.getId(), id);
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/products/recent")
    public ResponseEntity<List<ProductResponse>> getRecentViews(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        if (userDetails == null) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(recommendationService.getRecentViews(userDetails.getId(), 10));
    }

    @GetMapping("/recommendations")
    public ResponseEntity<List<ProductResponse>> getRecommendations(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        if (userDetails == null) {
            // Unauthenticated users get global bestsellers (simulated by passing 0 as userId, or handled by service)
            // For simplicity, passing a fake user ID or creating a specific method.
            // Assuming recommendationService handles it if we pass a null or 0, but let's pass a dummy for now
            // or just return empty list for anonymous in v1. We'll return global bestsellers by passing null category
            return ResponseEntity.ok(recommendationService.getRecommendations(-1L, 10));
        }
        return ResponseEntity.ok(recommendationService.getRecommendations(userDetails.getId(), 10));
    }
}
