# Phase 3: Shopping Cart Persistence - Research

**Researched:** 2026-04-05
**Domain:** Cart & Order (CART)
**Confidence:** HIGH

## Summary

This phase focuses on the persistent management of shopping carts for both registered users and guests. Key challenges include seamless merging of guest carts into user accounts upon login, preventing database bloat via scheduled cleanup of abandoned carts, and ensuring high performance by avoiding N+1 queries during cart retrieval.

**Primary recommendation:** Use a unified `CartService` interface that accepts a `CartIdentifier` (wrapping both `userId` and `guestId`) to maintain abstraction, and implement multi-server-safe scheduled cleanup using `ShedLock`.

<user_constraints>
## User Constraints (from 3-CONTEXT.md)

### Locked Decisions
- **Storage**: PostgreSQL 16 사용.
- **Abstraction**: `CartService` 인터페이스를 통해 향후 Redis 도입 시 코드 변경 최소화.
- **Identification**:
    - 회원: `user_id` (UUID) 기반 매핑.
    - 비회원: 클라이언트가 발급한 `guest_cart_id` (UUID) 기반 관리.
- **Merge Strategy**: 로그인 시점에 클라이언트가 `guest_cart_id`를 전달하면 병합 수행.
    - 동일 상품이 있을 경우: 수량 합산 (단, 최대 수량 제한 준수).
    - 새로운 상품일 경우: 회원 장바구니로 아이템 이동.
    - 병합 완료 후 비회원 장바구니 데이터 삭제.
- **Retention Period**: 7일 (마지막 수정일 기준).
- **Stock Validation**: 장바구니 추가/수정 시 실시간 재고 확인.
- **Quantity Limits**:
    - 개별 품목 최대 수량: 99개.
    - 장바구니 최대 품목(Unique Products) 수: 50개.

### the agent's Discretion
- `@Scheduled` 사용 시 여러 서버 인스턴스에서의 중복 실행 방지 대책 (ShedLock 등).
- 장바구니 조회 시 N+1 문제를 방지하기 위한 Fetch Join 또는 DTO 직접 조회 쿼리 최적화.

### Deferred Ideas (OUT OF SCOPE)
- Real-time inventory tracking with external ERP (from REQUIREMENTS.md).
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| CART-01 | User can add/remove products to/from a persistent cart. | `CartService` interface and PostgreSQL implementation with `CartIdentifier`. |
| CART-02 | User can update cart item quantities. | `CartService.updateItem` with stock validation logic. |
</phase_requirements>

## Standard Stack

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Spring Boot | 3.4.3 | Application Framework | Project standard. |
| ShedLock | 6.3.1 | Multi-server Scheduling | Prevents concurrent task execution in distributed environments. |
| Querydsl | 5.1.0 | Type-safe Queries | Used for efficient retrieval and JOINs. |

### Supporting
| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| shedlock-provider-jdbc-template | 6.3.1 | DB Lock Provider | Used with PostgreSQL for locking. |

**Installation:**
```xml
<dependency>
    <groupId>net.javacrumbs.shedlock</groupId>
    <artifactId>shedlock-spring</artifactId>
    <version>6.3.1</version>
</dependency>
<dependency>
    <groupId>net.javacrumbs.shedlock</groupId>
    <artifactId>shedlock-provider-jdbc-template</artifactId>
    <version>6.3.1</version>
</dependency>
```

## Architecture Patterns

### Recommended Project Structure
```
src/main/java/com/shop/domain/cart/
├── Cart.java                # Entity (User/Guest identification)
├── CartItem.java            # Entity (Product, Quantity)
├── CartRepository.java      # JPA Repository
├── CartRepositoryCustom.java # Querydsl interface
├── CartRepositoryImpl.java   # Querydsl implementation (Fetch Join)
├── CartService.java         # Interface for Redis-readiness
├── PostgresCartService.java  # RDB implementation
├── CartIdentifier.java      # DTO wrapper for UserID/GuestID
└── dto/                     # Request/Response DTOs
```

### Pattern 1: Multi-server Safe Scheduling (ShedLock)
**What:** Uses a dedicated database table (`shedlock`) to ensure only one instance runs a task at a time.
**When to use:** Daily cleanup of abandoned carts (older than 7 days).

### Pattern 2: Interface-based Service (Strategy Pattern)
**What:** `CartService` as an interface, with `PostgresCartService` as the current implementation.
**Why:** Eases the transition to Redis in the future by ensuring business logic doesn't depend on JPA-specific features like `EntityManager` directly.

### Anti-Patterns to Avoid
- **Implicit Domain Logic in Controllers:** Keep merge logic and quantity validation in `CartService`.
- **N+1 for Product Images:** Fetching cart items often leads to N queries for each product's primary image. Use Querydsl `fetchJoin()` with an `on` condition for `isPrimary`.

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Distributed Locking | Custom DB flags | ShedLock | Handles edge cases like node crashes and clock drift. |
| Cart Identifiers | Passing many params | `CartIdentifier` | Cleaner API and easier to expand (e.g., adding session keys). |
| Stock Check | Direct DB updates | `ProductService` call | Catalog domain owns stock logic; Cart domain just validates. |

## Common Pitfalls

### Pitfall 1: Race Conditions in Quantity Updates
**What goes wrong:** Two concurrent requests to "Add to Cart" might overwrite each other if not handled carefully.
**How to avoid:** Use atomic updates or optimistic locking (`@Version`) on the `Cart` or `CartItem` entity.

### Pitfall 2: Memory Leak in Merge Logic
**What goes wrong:** Loading thousands of guest cart items into memory for a single user (edge case, but possible).
**How to avoid:** Enforce unique item limits (50) and merge in a single transaction.

## Code Examples

### Querydsl Fetch Join with Primary Image
```java
// Source: Community Best Practices for 1:N with primary filter
public List<CartItem> findCartItems(CartIdentifier identifier) {
    QCartItem cartItem = QCartItem.cartItem;
    QProduct product = QProduct.product;
    QProductImage image = QProductImage.productImage;

    return queryFactory.selectFrom(cartItem)
            .join(cartItem.product, product).fetchJoin()
            .leftJoin(product.images, image)
                .on(image.isPrimary.isTrue())
                .fetchJoin()
            .where(identifierPredicate(identifier))
            .fetch();
}
```

### ShedLock Configuration
```java
@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "10m")
public class ShedLockConfig {
    @Bean
    public LockProvider lockProvider(DataSource dataSource) {
        return new JdbcTemplateLockProvider(
            JdbcTemplateLockProvider.Configuration.builder()
                .withJdbcTemplate(new JdbcTemplate(dataSource))
                .usingDbTime()
                .build()
        );
    }
}
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| Session-based Carts | UUID-based Persistent Carts | Modern E-commerce | Allows carts to persist across devices and browser restarts. |
| Quartz Scheduler | Spring @Scheduled + ShedLock | Spring Boot 2+ | Simpler setup for non-complex distributed scheduling. |

## Open Questions

1. **How to handle `guest_cart_id` on the frontend?**
   - Recommendation: Use a persistent UUID stored in `localStorage` and sent via an `X-Guest-Cart-Id` header or a cookie.
2. **Should we delete the User Cart too after 7 days?**
   - Context says "7일 경과 데이터 자동 삭제" (7-day old data automatic deletion).
   - Recommendation: Only delete `guest_cart_id` entries automatically. Keeping registered user carts is better UX, but follow the 7-day rule if explicitly required for all.

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| PostgreSQL | Data layer | ✓ | 16.2 | — |
| ShedLock | Multi-server scheduling | ✗ | — | Standard @Scheduled (single-node only) |

**Missing dependencies with no fallback:**
- None. ShedLock will be added to `pom.xml` in Phase 3.

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Framework | JUnit 5 + Mockito + Testcontainers (PostgreSQL) |
| Config file | `src/test/resources/application-test.yml` |
| Quick run command | `./mvnw test -Dtest=Cart*` |
| Full suite command | `./mvnw test` |

### Phase Requirements → Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| CART-01 | Add product to cart (Guest/User) | integration | `./mvnw test -Dtest=CartIntegrationTest::addItem` | ❌ Wave 0 |
| CART-02 | Merge guest cart on login | integration | `./mvnw test -Dtest=CartIntegrationTest::mergeCart` | ❌ Wave 0 |

## Sources

### Primary (HIGH confidence)
- Spring Boot 3.4 Official Documentation - Scheduling & Data JPA.
- ShedLock GitHub (net-javacrumbs/ShedLock) - Multi-server configuration.
- 3-CONTEXT.md - Project-specific decisions.

### Secondary (MEDIUM confidence)
- Baeldung: Guide to ShedLock - Implementation details.
- Querydsl Documentation - Fetch join best practices.
