# Phase 3 Context: Shopping Cart Persistence

## 결정된 사항 (Decisions)

### 1. 장바구니 아키텍처 (RDB & Migration Ready)
- **Storage**: PostgreSQL 16 사용.
- **Abstraction**: `CartService` 인터페이스를 통해 향후 Redis 도입 시 코드 변경 최소화.
- **Identification**:
    - 회원: `user_id` (UUID) 기반 매핑.
    - 비회원: 클라이언트가 발급한 `guest_cart_id` (UUID) 기반 관리.

### 2. 비회원 장바구니 병합 (Merge Strategy)
- **Trigger**: 로그인 시점에 클라이언트가 `guest_cart_id`를 전달하면 병합 수행.
- **Logic**:
    - 동일 상품이 있을 경우: 수량 합산 (단, 최대 수량 제한 준수).
    - 새로운 상품일 경우: 회원 장바구니로 아이템 이동.
    - 병합 완료 후 비회원 장바구니 데이터 삭제.

### 3. 데이터 수명 관리 (Expiration)
- **Retention Period**: 7일 (마지막 수정일 기준).
- **Implementation**: Spring `@Scheduled` 작업을 통한 주기적(Daily) 만료 데이터 삭제.

### 4. 검증 및 제약 사항
- **Stock Validation**: 장바구니 추가/수정 시 실시간 재고 확인.
- **Quantity Limits**:
    - 개별 품목 최대 수량: 99개.
    - 장바구니 최대 품목(Unique Products) 수: 50개.
- **Read Response**: 상품명, 가격, 메인 이미지 URL을 포함한 `CartItemResponse` 제공.

### 5. API 설계
- `GET /api/cart`: 현재 사용자의 장바구니 목록 조회 (회원/비회원 공용).
- `POST /api/cart`: 상품 추가 (재고 검증 포함).
- `PUT /api/cart/items/{itemId}`: 수량 수정.
- `DELETE /api/cart/items/{itemId}`: 특정 아이템 삭제.
- `POST /api/cart/merge`: 비회원 장바구니 수동 병합 (필요 시).

## 조사 및 확인이 필요한 사항 (To Investigate)
- `@Scheduled` 사용 시 여러 서버 인스턴스에서의 중복 실행 방지 대책 (ShedLock 등).
- 장바구니 조회 시 N+1 문제를 방지하기 위한 Fetch Join 또는 DTO 직접 조회 쿼리 최적화.

## 제약 사항 (Constraints)
- 모든 ID는 UUID v7 형식을 유지할 것.
- 비회원 장바구니 식별자(`guest_cart_id`)는 HTTP Header 또는 Request Body를 통해 유연하게 전달 가능해야 함.
