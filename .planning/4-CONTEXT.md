# Phase 4 Context: Checkout & Order Management

## 결정된 사항 (Decisions)

### 1. 주문 및 재고 트랜잭션 (Inventory First)
- **Stock Deduction**: 주문 생성 시점에 즉시 재고를 차감함 (v1 Mock 결제 최적화).
- **Payment Mocking**: 결제 성공을 가정하여 주문 완료(`COMPLETED`) 상태로 즉시 전환.
- **Cancellation**: 주문 취소 시 차감된 재고를 다시 상품 테이블로 복구함.

### 2. 주문 데이터 구조
- **Order Number Format**: `yyyyMMdd-ShortUUID` (예: 20240405-A1B2C3D4).
- **Order Statuses**: `PENDING`, `COMPLETED`, `SHIPPING`, `DELIVERED`, `CANCELLED`.
- **Shipping Info**: 주문별 수령인(Receiver Name), 연락처(Phone), 주소(Address) 별도 저장.
- **Order Items**: 주문 당시의 가격(Snapshot Price)과 수량을 저장.

### 3. 결제 정보 (Mock)
- **Table**: `payments` 테이블을 별도로 두어 결제 이력 관리.
- **Fields**: 결제수단(Method), 결제금액(Amount), 결제상태(Status), 결제일시(Paid At).

### 4. API 설계
- `POST /api/orders`: 장바구니 기반 주문 생성 및 결제 (Checkout).
- `GET /api/orders`: 현재 사용자의 주문 이력 목록 조회.
- `GET /api/orders/{id}`: 특정 주문 상세 조회 (상품 상세 정보 포함).
- `POST /api/orders/{id}/cancel`: 주문 취소 및 재고 복구.

## 조사 및 확인이 필요한 사항 (To Investigate)
- 동시 주문 시 재고 부족 문제를 방지하기 위한 비관적 락(Pessimistic Lock) 또는 낙관적 락(Optimistic Lock) 적용 검토.
- 주문 번호 생성 시 중복 방지를 위한 최적의 유틸리티 로직.
- 장바구니 아이템을 주문으로 변환한 후 장바구니 비우기 트랜잭션 처리.

## 제약 사항 (Constraints)
- 모든 테이블의 PK는 UUID v7 형식을 유지함.
- 주문 취소는 `COMPLETED` 상태에서만 가능하도록 제한 (배송 시작 전).
