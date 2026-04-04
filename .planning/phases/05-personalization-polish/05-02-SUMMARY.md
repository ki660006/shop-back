# Phase 5 Wave 2 Summary

## Objective
Implement core personalization features (recommendations, recent views) and application-level caching.

## Tasks Completed
1. **Hybrid Recommendation Engine & Recent Views**:
   - Implemented `RecentViewRepositoryCustom` and `RecentViewRepositoryCustomImpl` using Querydsl to fetch recent products, recent categories, ordered categories, and top-selling products.
   - Created `RecommendationService` with methods for recording views, getting recent views, and getting hybrid recommendations (recent orders/views categories -> top-selling -> fallback to global).
   - Created `RecommendationController` mapped to `/api/products/{id}/view`, `/api/products/recent`, and `/api/recommendations`.
2. **Enable Caching**:
   - Created `CacheConfig` with `@EnableCaching` and `ConcurrentMapCacheManager`.
   - Added `@Cacheable(value = "categoryTree", key = "'root'")` to `CategoryService.getCategoryTree()`.
3. **Scheduled Cleanup Batch**:
   - Created `RecentViewCleanupBatch` to schedule daily cleanup at 3 AM.
   - Used `@SchedulerLock(name = "recentViewCleanup")` to ensure safe distributed execution.
   - Utilized a native PostgreSQL query `ROW_NUMBER() OVER` to delete all but the 50 most recent views per user.

## Notes
The execution was manually completed due to agent turn limits.
