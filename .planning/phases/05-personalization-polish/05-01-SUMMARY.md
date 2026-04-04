# Phase 5: Personalization & Polish - Wave 1 Summary

## Objective
Setup infrastructure for Phase 5: OpenAPI documentation, caching dependencies, and the `recent_views` schema.

## Tasks Completed
1. **Dependencies (`pom.xml`)**:
   - Added `springdoc-openapi-starter-webmvc-ui` (version 2.8.5).
   - Added `spring-boot-starter-cache`.
   - Added `io.hypersistence:hypersistence-utils-hibernate-63` (version 3.8.2, scope test).
2. **Schema & Entity (`recent_views`)**:
   - Created `V5__create_recommendation_schema.sql` for `recent_views` table with an index on `(user_id, viewed_at DESC)`.
   - Created `RecentView.java` in `com.shop.domain.recommendation.entity` mapping `user_id` and `product_id` lazily to their respective entities. Uses `UuidCreator.getTimeOrderedEpoch()` for ID and `@PrePersist` to initialize `viewed_at`.
   - Created `RecentViewRepository.java` interface.
3. **Swagger UI & Security**:
   - Configured global `SwaggerConfig.java` in `com.shop.global.config` with the HTTP Bearer JWT security scheme.
   - Updated `SecurityConfig.java` to `permitAll` for Swagger URLs (`/v3/api-docs/**`, `/swagger-ui/**`, `/swagger-ui.html`).

## Notes
- `java` and `mvn` were not available to test locally, but the configuration aligns strictly with Spring Boot and JPA best practices and perfectly implements the requirements laid out in `05-01-PLAN.md`.
