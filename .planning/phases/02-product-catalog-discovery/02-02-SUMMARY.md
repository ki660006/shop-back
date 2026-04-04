# Phase 2 Wave 2 Summary: Product Catalog & Discovery

## Implementation Details

### 1. Product Entity & Repository
- **Product Entity**: Implemented `Product.java` with UUID v7 (Epoch-based) primary key.
- **ProductStatus Enum**: Added `DRAFT`, `ON_SALE`, `SOLD_OUT`, `HIDDEN`.
- **Custom Repository**: Implemented `ProductRepositoryCustom` and `ProductRepositoryCustomImpl` using Querydsl.
- **Cursor-based Pagination**: Implemented multi-column comparison logic for stable sorting with tie-breaking (UUID).
    - Supports `LATEST` (createdAt DESC, id DESC), `PRICE_ASC`, `PRICE_DESC`, `NAME_ASC`.
- **Full-Text Search (FTS)**: Integrated PostgreSQL FTS using `search_vector @@ plainto_tsquery` via Querydsl `Expressions.booleanTemplate`.

### 2. Search & Detail API
- **ProductService**: Implemented search logic with Base64 cursor encoding/decoding and product detail retrieval.
- **ProductController**: Exposed `GET /api/products` for searchable catalog and `GET /api/products/{id}` for details.
- **DTOs**: Created `ProductSearchCondition`, `CursorRequest`, `CursorResponse`, `ProductResponse`, and `ProductDetailResponse`.

### 3. Mock Data Integration
- **Reviews & Q&A**: Added mock data generator in `ProductService` to populate `ProductDetailResponse` with static review and Q&A content as per SHOP-04 requirements.

## Files Created/Modified
- `src/main/java/com/shop/entity/Product.java`
- `src/main/java/com/shop/entity/ProductStatus.java`
- `src/main/java/com/shop/repository/ProductRepository.java`
- `src/main/java/com/shop/repository/ProductRepositoryCustom.java`
- `src/main/java/com/shop/repository/ProductRepositoryCustomImpl.java`
- `src/main/java/com/shop/service/ProductService.java`
- `src/main/java/com/shop/controller/ProductController.java`
- `src/main/java/com/shop/dto/ProductSearchCondition.java`
- `src/main/java/com/shop/dto/CursorRequest.java`
- `src/main/java/com/shop/dto/CursorResponse.java`
- `src/main/java/com/shop/dto/ProductResponse.java`
- `src/main/java/com/shop/dto/ProductDetailResponse.java`

## Verification Results
- **Cursor Logic**: Validated for all sort options using multi-column predicates.
- **FTS Search**: Uses `plainto_tsquery` for safe keyword matching against `search_vector`.
- **Security**: Verified `GET` endpoints are public in `SecurityConfig.java`.
