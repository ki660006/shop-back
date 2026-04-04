# Phase 05: Personalization & Polish - Research

**Researched:** 2026-04-05
**Domain:** Recommendation Engine, Caching, OpenAPI Documentation, Performance Tuning (N+1)
**Confidence:** HIGH

## Summary
The Personalization & Polish phase introduces a hybrid recommendation engine, automated API documentation, and performance tuning (caching + N+1 elimination). We will leverage Springdoc for OpenAPI 2.x/3.x, Spring's `@Cacheable` for the static category tree, and Querydsl for complex recommendation sorting. The `recent_views` tracking will be backed by UUIDv7 primary keys to natively cluster chronologically and managed via scheduled batch cleanup to ensure scalable personalized data retention.

**Primary recommendation:** Use `@Async` and a daily `@Scheduled` ShedLock job for `recent_views` DB cleanup instead of strict synchronous limits on every request. Use Hypersistence Utils in tests to automatically catch and enforce N+1 query regressions.

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions
None - CONTEXT.md not present.

### the agent's Discretion
None - CONTEXT.md not present.

### Deferred Ideas (OUT OF SCOPE)
None - CONTEXT.md not present.
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| REC-01 | User can see personalized product recommendations based on simple heuristics | Addressed by Hybrid Recommendation Querydsl query & `recent_views` tracking strategy. |
</phase_requirements>

## Standard Stack

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| `springdoc-openapi-starter-webmvc-ui` | `2.8.5` | Swagger/OpenAPI | Native Spring Boot 3 & Security 6 support |
| `spring-boot-starter-cache` | `3.4.3` | Category caching | Native integration for `@Cacheable` |
| `uuid-creator` | `6.0.0` | UUID v7 generation | Already in `pom.xml`, generates time-ordered UUIDs |
| `hypersistence-utils-hibernate-63` | `3.8.2` | N+1 Testing | Best library for `SQLStatementCountValidator` |

**Installation:**
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.5</version>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>
<dependency>
    <groupId>io.hypersistence</groupId>
    <artifactId>hypersistence-utils-hibernate-63</artifactId>
    <version>3.8.2</version>
    <scope>test</scope>
</dependency>
```

## Architecture Patterns

### OpenAPI & Spring Security 6 Integration
**What:** Exposing Swagger UI securely with JWT support.
**When to use:** In standard API projects.
**Example:**
```java
@Configuration
@SecurityScheme(
  name = "bearerAuth",
  type = SecuritySchemeType.HTTP,
  bearerFormat = "JWT",
  scheme = "bearer"
)
public class OpenApiConfig {}
```
*Note: In `SecurityConfig.java`, you must explicitly permit the paths: `"/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html"`.*

### Spring Cache for Category Tree
**What:** In-memory caching for read-heavy hierarchy data.
**When to use:** `CategoryService.getCategoryTree()`.
**Example:**
```java
@EnableCaching // On Application class or Config

// On Service:
@Cacheable(value = "categoryTree", key = "'root'")
public List<CategoryResponse> getCategoryTree() { ... }

@CacheEvict(value = "categoryTree", allEntries = true)
public void createCategory(...) { ... }
```

### Recent Views: UUID v7 & Retention Strategy
**What:** Storing user product views efficiently.
**When to use:** Whenever a product detail is fetched.
**How to avoid scaling issues:** Do not enforce the 50-row limit synchronously on every `INSERT` or via DB triggers (causes contention). Instead, allow unbounded inserts and use a background job to prune old rows.
**Example:**
```java
// Entity
@Id
private UUID id = UuidCreator.getTimeOrderedEpoch(); // UUID v7
```

### Hybrid Recommendation Querydsl Pattern
**What:** Recommending top-selling products in recently viewed categories.
**When to use:** Home page or recommendations section.
**Example:**
```java
// Find top 10 best-selling products in given categories
queryFactory.selectFrom(product)
    .leftJoin(orderItem).on(orderItem.product.eq(product))
    .where(product.category.id.in(recentCategoryIds))
    .groupBy(product.id)
    .orderBy(orderItem.quantity.sum().desc(), product.id.desc())
    .limit(10)
    .fetch();
```

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| UUID v7 generation | Custom bit-shifting logic | `uuid-creator` | Already in `pom.xml`, guarantees RFC 4122 collision resistance and monotonic sorting |
| Automated API Docs | Custom HTML or Postman exports | `springdoc-openapi-starter-webmvc-ui` | Automatically reads Spring MVC annotations and stays perfectly in sync with the codebase |
| N+1 Testing | Custom log parsing scripts | `hypersistence-utils` | Intercepts JDBC natively, failing tests cleanly with `assertSelectCount(1)` |

## Common Pitfalls

### Pitfall 1: Synchronous `recent_views` Pruning
**What goes wrong:** Adding `DELETE FROM recent_views WHERE ... LIMIT ...` in the same transaction as the `INSERT`.
**Why it happens:** Attempting to strictly keep exactly 50 rows per user at all times.
**How to avoid:** Use a `@Scheduled` ShedLock job to periodically prune rows, e.g.:
```sql
DELETE FROM recent_views
WHERE id NOT IN (
    SELECT id FROM (
        SELECT id, ROW_NUMBER() OVER(PARTITION BY user_id ORDER BY viewed_at DESC) as rn
        FROM recent_views
    ) sub WHERE rn <= 50
);
```
**Warning signs:** High database write contention during product views.

### Pitfall 2: N+1 in Existing APIs
**What goes wrong:** Fetching Cart or Order details triggers many small queries.
**Why it happens:** JPA lazy loading on collections like `Cart.items` or `Order.items` when mapped to DTOs without `JOIN FETCH`.
**How to avoid:** Add `@EntityGraph(attributePaths = {"items", "items.product"})` to repository methods, and add `SQLStatementCountValidator` in `IntegrationTest` files.
**Warning signs:** High query execution count in Hibernate statistics logs when fetching a single aggregate.

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Framework | JUnit 5 + Spring Boot Test |
| Config file | `pom.xml` |
| Quick run command | `mvn test -Dtest=*IntegrationTest` |
| Full suite command | `mvn test` |

### Phase Requirements → Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| REC-01 | Recommend products | integration | `mvn test -Dtest=RecommendationIntegrationTest` | ❌ Phase 5 |

### Wave 0 Gaps
- [ ] `src/test/java/com/shop/RecommendationIntegrationTest.java` — covers REC-01
- [ ] Install `springdoc-openapi-starter-webmvc-ui` dependency
- [ ] Install `hypersistence-utils-hibernate-63` test dependency for query counting

## Sources

### Primary (HIGH confidence)
- Official Springdoc: `https://springdoc.org/`
- Official Hypersistence Utils: `https://vladmihalcea.com/hypersistence-utils/`
- UUID Creator Documentation: `https://github.com/f4b6a3/uuid-creator`

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH - Verified via Maven Central and Spring Boot ecosystem standard practices.
- Architecture: HIGH - Common enterprise patterns for Spring Boot 3.x and JPA.
- Pitfalls: HIGH - Documented standard issues in scaling e-commerce recommendations and JPA queries.

**Research date:** 2026-04-05
**Valid until:** 2026-05-05