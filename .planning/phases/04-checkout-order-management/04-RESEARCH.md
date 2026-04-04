# Phase 4: Checkout & Order Management - Research

**Researched:** 2026-04-05
**Domain:** Checkout process, Inventory management, Order generation, Bulk operations, Mock payments
**Confidence:** HIGH

## Summary

Phase 4는 사용자의 장바구니 데이터를 주문 데이터로 변환하고, 결제 과정을 시뮬레이션하며, 이 과정에서 발생할 수 있는 데이터 정합성(재고 부족 등) 문제를 해결하는 것을 목표로 합니다. 핵심은 **트랜잭션 원자성**과 **동시성 제어**입니다.

**Primary recommendation:** Spring Boot의 `@Transactional`과 JPA의 `PESSIMISTIC_WRITE` 락을 조합하여 재고 차감의 정합성을 보장하고, PostgreSQL의 `reWriteBatchedInserts` 설정을 통해 주문 생성 성능을 최적화합니다.

<user_constraints>
## User Constraints (from 4-CONTEXT.md)

### Locked Decisions
- **Stock Deduction**: 주문 생성 시점에 즉시 재고를 차감함 (v1 Mock 결제 최적화).
- **Payment Mocking**: 결제 성공을 가정하여 주문 완료(`COMPLETED`) 상태로 즉시 전환.
- **Cancellation**: 주문 취소 시 차감된 재고를 다시 상품 테이블로 복구함.
- **Order Number Format**: `yyyyMMdd-ShortUUID` (예: 20240405-A1B2C3D4).
- **Order Statuses**: `PENDING`, `COMPLETED`, `SHIPPING`, `DELIVERED`, `CANCELLED`.
- **Shipping Info**: 주문별 수령인(Receiver Name), 연락처(Phone), 주소(Address) 별도 저장.
- **Order Items**: 주문 당시의 가격(Snapshot Price)과 수량을 저장.
- **Table**: `payments` 테이블을 별도로 두어 결제 이력 관리.
- **Fields**: 결제수단(Method), 결제금액(Amount), 결제상태(Status), 결제일시(Paid At).

### the agent's Discretion
- 동시 주문 시 재고 부족 문제를 방지하기 위한 비관적 락(Pessimistic Lock) 또는 낙관적 락(Optimistic Lock) 적용 검토.
- 주문 번호 생성 시 중복 방지를 위한 최적의 유틸리티 로직.
- 장바구니 아이템을 주문으로 변환한 후 장바구니 비우기 트랜잭션 처리.

### Deferred Ideas (OUT OF SCOPE)
- OAuth integration (Google, GitHub).
- Real-time PG (Payment Gateway) integration.
- Multi-language and multi-currency support.
- Advanced recommendation engine (collaborative filtering).
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| ORDER-01 | User can place an order from their cart (checkout). | 트랜잭션 원자성 및 재고 차감 로직 연구 |
| ORDER-02 | User can view details of a specific order. | 주문 상세 조회 및 Snapshot Price 저장 구조 연구 |
| USER-01 | User can view their order history. | 주문 이력 조회 API 및 인덱스 최적화 연구 |
</phase_requirements>

## Standard Stack

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Spring Data JPA | (Spring Boot 3.4.3) | ORM 및 데이터 접근 | 표준 자바 영속성 기술 |
| PostgreSQL JDBC | (Latest) | Database 연동 | `reWriteBatchedInserts` 지원 |
| Uuid-Creator | 6.0.0 | UUID v7 생성 | 시간 기반 정렬 가능한 UUID 지원 |

**Installation:**
이미 `pom.xml`에 포함되어 있음.

## Architecture Patterns

### Recommended Project Structure
```
src/main/java/com/shop/domain/order/
├── controller/      # OrderController
├── dto/             # OrderRequest, OrderResponse, CheckoutRequest
├── entity/          # Order, OrderItem, OrderStatus (Enum)
├── repository/      # OrderRepository, OrderItemRepository
└── service/         # OrderService, OrderNumberGenerator
src/main/java/com/shop/domain/payment/
├── entity/          # Payment, PaymentMethod (Enum)
├── service/         # PaymentService (Interface), MockPaymentService
└── repository/      # PaymentRepository
```

### Pattern 1: Pessimistic Locking for Inventory
**What:** `SELECT ... FOR UPDATE`를 사용하여 재고 행에 락을 걸어 동시성 제어.
**When to use:** 주문이 몰리는 시점에 동일 상품의 재고가 동시에 차감되는 것을 방지.
**Example:**
```typescript
// Repository
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT p FROM Product p WHERE p.id = :id")
Optional<Product> findByIdWithLock(@Param("id") UUID id);

// Service
@Transactional
public void decreaseStock(UUID productId, int quantity) {
    Product product = productRepository.findByIdWithLock(productId)
        .orElseThrow(() -> new EntityNotFoundException("Product not found"));
    
    if (product.getStockQuantity() < quantity) {
        throw new OutOfStockException("Insufficient stock");
    }
    
    product.setStockQuantity(product.getStockQuantity() - quantity);
    // 갱신 후 트랜잭션 종료 시 락 해제
}
```

### Anti-Patterns to Avoid
- **Implicit Stock Update:** 별도의 락 없이 `product.setStockQuantity(product.getStockQuantity() - n)`을 호출하면 갱신 유실(Lost Update) 발생 가능.
- **Cart-to-Order N+1:** 장바구니 아이템 순회 시 각 상품의 정보를 개별 쿼리로 가져오지 말고, `JOIN FETCH` 또는 `IN` 절을 사용하여 한 번에 조회.

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| UUID Generation | Custom UUID logic | `UuidCreator.getTimeOrderedEpoch()` | UUID v7의 정렬 이점 및 표준 준수 |
| Bulk Insert | Native `INSERT` loop | `reWriteBatchedInserts=true` | JDBC 드라이버 레벨에서 다중 값 인서트로 변환하여 성능 극대화 |
| Date Formatting | Manual string concat | `DateTimeFormatter` | Thread-safe하고 표준화된 날짜 처리 |

## Common Pitfalls

### Pitfall 1: JDBC Batch Insert Silence
**What goes wrong:** `batch_size`를 설정해도 실제로는 단건 인서트가 여러 번 발생함.
**Why it happens:** PostgreSQL JDBC 드라이버의 `reWriteBatchedInserts` 옵션이 꺼져 있거나, JPA의 `GenerationType.IDENTITY`를 사용하기 때문.
**How to avoid:** JDBC URL에 옵션 추가 및 애플리케이션 생성 ID(UUID v7) 사용.

### Pitfall 2: Locking Deadlock
**What goes wrong:** 여러 상품을 주문할 때 트랜잭션 간 데드락 발생.
**Why it happens:** 트랜잭션 A가 상품 1, 2를 잠그려 하고, 트랜잭션 B가 상품 2, 1을 잠그려 할 때 발생.
**How to avoid:** 주문 아이템의 상품 ID를 항상 일정한 순서(예: 오름차순)로 정렬한 뒤 락을 획득하도록 구현.

## Code Examples

### Order Number Generation (yyyyMMdd-ShortUUID)
```java
public class OrderNumberGenerator {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public static String generate() {
        String datePart = LocalDate.now().format(DATE_FORMATTER);
        String uuidPart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return String.format("%s-%s", datePart, uuidPart);
    }
}
```

### Mock Payment Service
```java
public interface PaymentService {
    PaymentResult process(PaymentRequest request);
}

@Service
public class MockPaymentService implements PaymentService {
    @Override
    public PaymentResult process(PaymentRequest request) {
        // 실제 연동 없이 항상 성공 반환 (결제 이력은 DB에 저장)
        return new PaymentResult(true, UUID.randomUUID().toString());
    }
}
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| UUID v4 (Random) | UUID v7 (Time-ordered) | 2024 | 인덱스 성능 향상 및 정렬 용이 |
| JPA Save Loop | JDBC Batch Insert | Always | 대량 인서트 성능 5~10배 향상 |

## Open Questions

1. **ShortUUID의 충돌 가능성**
   - 현재 8자리 접미사를 사용하면 하루 수백만 건 주문 시 충돌 위험이 있으나, 현재 규모에서는 안전함. 더 높은 안전성이 필요하면 Base62 인코딩 사용 권장.
   - Recommendation: 우선 8자리 서브스트링으로 시작하고, 충돌 발생 시 전체 UUID Base62 변환 고려.

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| JDK 21 | Runtime | ✓ | 21.0.2 | — |
| PostgreSQL | Data Store | ✓ | 16.2 | — |
| Maven | Build | ✓ | 3.9.6 | — |

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Framework | JUnit 5, AssertJ |
| Config file | `src/test/resources/application-test.yml` |
| Quick run command | `mvn test -Dtest=OrderIntegrationTest` |
| Full suite command | `mvn test` |

### Phase Requirements → Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| ORDER-01 | 재고 부족 시 주문 실패 및 롤백 확인 | Integration | `mvn test -Dtest=OrderIntegrationTest#checkout_fails_when_out_of_stock` | ❌ Wave 0 |
| ORDER-01 | 동시 주문 시 재고 정합성 유지 확인 | Concurrency | `mvn test -Dtest=OrderConcurrencyTest` | ❌ Wave 0 |
| ORDER-02 | 주문 상세 조회 시 상품 스냅샷 가격 확인 | Unit | `mvn test -Dtest=OrderServiceTest#order_contains_snapshot_price` | ❌ Wave 0 |

## Sources

### Primary (HIGH confidence)
- Spring Data JPA Reference - Locking
- PostgreSQL JDBC Driver Documentation - Batching
- RFC 9562 (UUID v7)

### Secondary (MEDIUM confidence)
- Baeldung - Guide to Hibernate Batch Inserts
- Various E-commerce Architecture blogs

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH - 표준 라이브러리 활용
- Architecture: HIGH - 일반적인 이커머스 패턴
- Pitfalls: HIGH - 재고 관리의 전형적인 이슈

**Research date:** 2026-04-05
**Valid until:** 2026-05-05
