# Phase 2: Product Catalog & Discovery - Research

**Researched:** 2025-04-04
**Domain:** PostgreSQL 16, Querydsl 5.x, Spring Boot 3.4, Full-Text Search, Hierarchical Data
**Confidence:** HIGH

## Summary

This phase focuses on a performant product catalog with advanced search and filtering capabilities. Key findings highlight the superiority of **UUID v7** (time-ordered) over v4 for indexing, the necessity of **Querydsl 5.x (Jakarta)** for Spring Boot 3 compatibility, and the efficiency of **Application-side bulk loading** for small-to-medium hierarchical category trees. PostgreSQL 16's Full-Text Search (FTS) will be optimized via **Generated Columns** and **GIN indexes** using the `pg_bigm` or standard `simple` configuration for Korean language support.

**Primary recommendation:** Use **UUID v7** for all primary keys to avoid index fragmentation and implement **Cursor-based Pagination** (Keyset pagination) with Base64-encoded cursors to provide a smooth, infinite-scroll experience.

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions
- **Primary Keys**: All IDs use `UUID` type.
- **Foreign Keys**: All references use `UUID` type.
- **Unique Keys**: `categories.slug`, `products.code`.
- **Category Hierarchy**: `UUID` 기반 `parent_id` 계층 구조.
- **Sorting Options**: `LATEST`, `PRICE_ASC`, `PRICE_DESC`, `NAME_ASC`.
- **Pagination Strategy**: **Cursor-based Pagination** (무한 스크롤).
- **Full-Text Search**: PostgreSQL Native FTS (`tsvector`, `tsquery`) with GIN indexes on `name` and `description`.
- **Media Strategy**: Local storage with UUID filenames.

### the agent's Discretion
- Querydsl 동적 Cursor 쿼리 구현 방식.
- UUID v7 사용 여부 (추천: v7).
- Cursor 인코딩/디코딩 로직.
- 카테고리 트리 변환 알고리즘 (추천: Application-side Bulk Load).

### Deferred Ideas (OUT OF SCOPE)
- Admin CMS / Back-office portal.
- Real-time inventory tracking with external ERP.
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| SHOP-01 | 카테고리 조회 | Verified Application-side bulk load for category tree. |
| SHOP-02 | 상품 검색 및 필터링 | Verified PostgreSQL FTS + Generated Column + GIN index. |
| SHOP-03 | 상품 상세 조회 | Standard JPA retrieval with UUID PK. |
| SHOP-04 | 리뷰 및 Q&A 노출 | Mock data support confirmed via DTO mapping. |
| CORE-01 | UUID 성능 최적화 | Recommended UUID v7 for sequential B-Tree performance. |
| CORE-02 | Cursor 기반 페이징 | Verified multi-column sort logic for price/name/id. |
</phase_requirements>

## Standard Stack

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Querydsl JPA | 5.1.0 (jakarta) | Type-safe Dynamic Query | Industry standard for Spring Data JPA. |
| pg_bigm | Extension | Korean N-gram FTS | Better recall for short Korean words than standard `tsvector`. |
| uuid-creator | 6.0.0 | UUID v7 Generation | High-performance sequential UUID generator. |

### Supporting
| Library | Version | Purpose | When to Use |
|---------|---------|---------|--------------|
| Apache Tika | 3.0.0 | MIME Type Verification | Secure file upload validation. |
| Spring Web | 6.2.x | Static Resource Serving | Native support for external file serving. |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| UUID v4 | UUID v7 | v4 causes random I/O and index bloat; v7 is sequential. |
| Offset Paging | Cursor Paging | Offset is O(N) and unstable; Cursor is O(log N) and stable. |
| Recursive CTE | Bulk Load Tree | CTE is complex; Bulk Load is faster for small e-commerce trees (<1k nodes). |

**Installation (Maven):**
```xml
<!-- Querydsl -->
<dependency>
    <groupId>com.querydsl</groupId>
    <artifactId>querydsl-jpa</artifactId>
    <version>5.1.0</version>
    <classifier>jakarta</classifier>
</dependency>

<!-- UUID v7 -->
<dependency>
    <groupId>com.github.f4b6a3</groupId>
    <artifactId>uuid-creator</artifactId>
    <version>6.0.0</version>
</dependency>
```

## Architecture Patterns

### Recommended Project Structure
```
src/main/java/com/shop/
├── controller/
│   ├── ProductController.java
│   └── CategoryController.java
├── service/
│   ├── ProductService.java
│   └── CategoryService.java (Tree logic)
├── repository/
│   ├── ProductRepository.java
│   └── ProductRepositoryCustom.java (Querydsl)
└── dto/
    ├── ProductSearchCondition.java
    └── CursorResponse.java
```

### Pattern 1: Multi-column Cursor Logic
**What:** Total ordering for cursor pagination using tie-breakers.
**When to use:** Infinite scroll with sorting (Price, Name, etc.).
**Logic:**
- `PRICE_ASC`: `(price > cursor.price) OR (price == cursor.price AND id > cursor.id)`
- `PRICE_DESC`: `(price < cursor.price) OR (price == cursor.price AND id < cursor.id)`
- `NAME_ASC`: `(name > cursor.name) OR (name == cursor.name AND id > cursor.id)`

### Pattern 2: Application-side Category Tree
**What:** Load all categories and stitch into a tree structure in Java.
**When to use:** Trees with < 1000 nodes (common for shopping malls).
**Benefit:** Zero N+1 issues, easy to cache globally.

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| UUID Generation | Custom byte logic | `uuid-creator` | Compliant with RFC 9562 (UUID v7). |
| File Sanitization | Simple string replace | `StringUtils.cleanPath` | Prevents sophisticated Path Traversal. |
| Korean FTS | Custom `LIKE %word%` | `pg_bigm` or FTS | `LIKE` is O(N); FTS with GIN is O(log N). |

## Common Pitfalls

### Pitfall 1: Random UUID Performance
**What goes wrong:** Database performance degrades as the table grows.
**Why it happens:** UUID v4 (random) causes constant B-Tree page splits and cache misses.
**How to avoid:** Use **UUID v7** (time-ordered).

### Pitfall 2: Querydsl Jakarta Namespace
**What goes wrong:** `NoClassDefFoundError: javax/persistence/Entity`.
**Why it happens:** Spring Boot 3 uses `jakarta.persistence`.
**How to avoid:** Use the `jakarta` classifier for all Querydsl dependencies.

### Pitfall 3: N+1 on Category Tree
**What goes wrong:** fetching a tree results in 100+ queries.
**Why it happens:** Lazy loading children recursively.
**How to avoid:** Bulk fetch all categories in one query and build tree in memory.

## Code Examples

### Querydsl Cursor Paging (Pseudo-code)
```java
// price > :lastPrice OR (price == :lastPrice AND id > :lastId)
BooleanExpression cursorCondition = product.price.gt(lastPrice)
    .or(product.price.eq(lastPrice).and(product.id.gt(lastId)));

return queryFactory
    .selectFrom(product)
    .where(cursorCondition, otherFilters)
    .orderBy(product.price.asc(), product.id.asc())
    .limit(pageSize + 1) // One extra to check hasNext
    .fetch();
```

### PostgreSQL Generated FTS Column
```sql
-- Source: PostgreSQL Official Docs / pg_bigm
ALTER TABLE products 
ADD COLUMN search_vector tsvector 
GENERATED ALWAYS AS (to_tsvector('simple', name || ' ' || description)) STORED;

CREATE INDEX idx_products_search_gin ON products USING GIN(search_vector);
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| UUID v4 (Random) | UUID v7 (Ordered) | RFC 9562 (2024) | Better DB performance. |
| Offset Pagination | Cursor Pagination | Modern Mobile UI | Faster, stable scrolling. |
| `javax.*` | `jakarta.*` | Spring Boot 3.0 | Java EE to Jakarta EE transition. |

## Open Questions

1. **Local vs S3 Storage:**
   - What we know: Local storage is planned for v1.
   - What's unclear: Deployment environment (Docker volume config?).
   - Recommendation: Use a configurable root path (`/uploads`) that can be mapped to a volume.

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| PostgreSQL 16 | UUID v7 / FTS | ✓ | 16.x | — |
| Java 21 | Virtual Threads | ✓ | 21.x | — |
| Maven | Dependency Mgmt | ✓ | 3.9.x | — |

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Framework | JUnit 5 + Testcontainers (PostgreSQL) |
| Config file | `src/test/resources/application-test.yml` |
| Quick run command | `mvn test -Dgroups=unit` |
| Full suite command | `mvn test` |

### Phase Requirements → Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| SHOP-01 | Hierarchical Tree | Unit | `mvn test -Dtest=CategoryServiceTest` | ❌ Wave 0 |
| SHOP-02 | Search Performance | Integration | `mvn test -Dtest=ProductSearchTest` | ❌ Wave 0 |
| CORE-02 | Cursor Paging | Integration | `mvn test -Dtest=PaginationTest` | ❌ Wave 0 |

## Sources

### Primary (HIGH confidence)
- RFC 9562: UUID version 7 specification.
- PostgreSQL 16 Release Notes: Generated columns and GIN index performance.
- Querydsl 5.1 Release Notes: Jakarta EE compatibility.

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH - UUID v7 and Querydsl 5.1 (Jakarta) are well-documented.
- Architecture: HIGH - Cursor pagination is a standard pattern for infinite scroll.
- Pitfalls: HIGH - UUID fragmentation and N+1 issues are classic database problems.

**Research date:** 2025-04-04
**Valid until:** 2025-06-04
