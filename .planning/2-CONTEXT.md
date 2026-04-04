# Phase 2 Context: Product Catalog & Discovery

## 결정된 사항 (Decisions)

### 1. 데이터베이스 구조 (UUID Standard)
- **Primary Keys**: 모든 테이블의 `id`는 `UUID` 타입을 사용함.
- **Foreign Keys**: 모든 참조 필드 역시 `UUID` 타입을 사용함.
- **Unique Keys**: `categories.slug`, `products.code` 등 식별 가능한 필드에 적절한 UK 설정.

### 2. 카테고리 및 상품 관리
- **Category Hierarchy**: `UUID` 기반의 `parent_id`를 가진 계층형 구조.
- **Product Status**: 
    - `ON_SALE`: 판매 중
    - `SOLD_OUT`: 품절
    - `HIDDEN`: 숨김 처리 (검색 및 목록 제외)
- **Sorting Options**:
    - `LATEST`: 최신순 (기본값)
    - `PRICE_ASC`: 가격 낮은 순
    - `PRICE_DESC`: 가격 높은 순
    - `NAME_ASC`: 이름순

### 3. 검색 및 페이징 (Cursor-based)
- **Pagination Strategy**: **Cursor-based Pagination** (무한 스크롤 최적화).
    - 정렬 기준에 따른 Cursor 조합 필드(예: `price` + `id` 등) 사용.
- **Full-Text Search**:
    - PostgreSQL Native FTS (`tsvector`, `tsquery`) 사용.
    - `name`과 `description`에 대한 GIN 인덱스 적용.

### 4. 미디어 처리 (File Upload)
- **Strategy**: 로컬 스토리지 저장 및 UUID 기반 파일명 관리.
- **Features**: 실제 파일 업로드 및 상품 이미지 매핑 기능 포함.

## 조사 및 확인이 필요한 사항 (To Investigate)
- Querydsl을 활용한 동적 Cursor 페이징 쿼리 구현 방식.
- UUID 사용 시 인덱스 파편화 방지를 위한 최적화 (PostgreSQL 16의 UUID 성능).
- 정렬 기준별 Cursor 생성 및 검증 로직.

## 제약 사항 (Constraints)
- **기존 Phase 1 연계**: Phase 1에서 생성된 `User` 및 `RefreshToken` 테이블의 ID 타입도 향후 리팩토링 시 `UUID`로 통일 고려 (Phase 2는 무조건 `UUID`로 시작).
- **Flyway**: 새로운 테이블 및 FTS 인덱스 설정은 모두 Flyway로 관리.
