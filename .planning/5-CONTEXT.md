# Phase 5 Context: Personalization & Polish

## 결정된 사항 (Decisions)

### 1. 개인화 추천 로직 (Heuristics)
- **혼합형(Hybrid) 추천**:
    1.  **1순위 (User-specific)**: 사용자의 최근 구매 이력(Order) 및 최근 본 상품(Recent Views) 카테고리를 기반으로 연관 상품 추천.
    2.  **2순위 (Global)**: 개인화 데이터가 부족할 경우, 누적 판매량(Order Items 수량) 기반의 '베스트셀러' 상품으로 채움.
- **최근 본 상품 추적 (Recently Viewed)**:
    - 서버 측 데이터베이스(`recent_views` 테이블)에 사용자가 조회한 상품 이력을 저장 및 관리 (최대 N개 유지 로직 포함).

### 2. API 문서화 (Documentation)
- **도구**: Springdoc OpenAPI (Swagger UI).
- **설정**: 모든 컨트롤러 메서드에 대한 명세 자동화 및 JWT(Bearer Token) 인증을 Swagger UI 내에서 바로 테스트할 수 있도록 Security Scheme 구성.

### 3. 성능 최적화 및 캐싱 (Performance Polish)
- **캐싱(Caching) 도입**: Spring `@EnableCaching` 활용 (v1은 기본 인메모리 캐시 사용).
    - 타겟: 계층형 카테고리 트리 조회 (`CategoryService`), 베스트셀러/추천 상품 목록 등 잦은 조회가 발생하는 읽기 전용 데이터.
- **N+1 문제 해결**: 기존 주문 내역 조회, 장바구니 조회, 카탈로그 검색 등에서 JPA N+1 문제가 발생하지 않도록 Fetch Join 적용 여부 최종 점검.

### 4. API 설계 (추가)
- `GET /api/recommendations`: 현재 사용자 맞춤형 추천 상품 목록 반환.
- `GET /api/products/recent`: 사용자가 최근 본 상품 목록 반환.
- `POST /api/products/{id}/view`: 사용자가 특정 상품을 조회했을 때 이력 저장 (ProductDetail API와 별도로 동작하거나 통합 가능).

## 조사 및 확인이 필요한 사항 (To Investigate)
- 최근 본 상품(`recent_views`) 테이블에 데이터가 무한정 쌓이지 않도록, 사용자당 최신 50개만 유지하는 DB 쿼리(또는 Trigger/배치) 최적화 방법.
- Spring Cache 사용 시 카테고리 추가/수정/삭제 이벤트 발생 시 캐시를 무효화(`@CacheEvict`)하는 전략.
- Springdoc OpenAPI 2.x와 Spring Security 6.x 호환성 및 JWT 설정 방법.

## 제약 사항 (Constraints)
- 모든 새로운 테이블의 PK는 UUID v7 형식을 유지함.
- 기존 아키텍처(도메인 기반 패키지 분리) 원칙을 엄격하게 준수.
