# Pitfalls Research

**Domain:** E-commerce Backend (Spring Boot)
**Researched:** 2026-04-05
**Confidence:** HIGH

## Critical Pitfalls

### Pitfall 1: The N+1 Query Problem in Hibernate

**What goes wrong:**
Fetching a list of entities (e.g., 50 orders) results in 51 database queries because the associated entities (e.g., order items) are fetched one by one.

**Why it happens:**
Lazy loading is the default. When iterating over a list and accessing a proxy property, Hibernate triggers a separate query for each child.

**How to avoid:**
Use `JOIN FETCH` in JPQL, Entity Graphs, or specify `@BatchSize`. Use Querydsl to build efficient joined queries.

**Warning signs:**
High number of SQL logs for a single request; slow performance on list endpoints.

**Phase to address:**
Phase 1 (Domain Model & Repository setup).

---

### Pitfall 2: Inventory Race Conditions

**What goes wrong:**
Two users purchase the same last item simultaneously, leading to overselling (negative inventory).

**Why it happens:**
Standard read-update-save flow without proper locking. Both transactions read "1 item left" and both save "0 items left".

**How to avoid:**
Use JPA Optimistic Locking (`@Version`) or Pessimistic Locking (`SELECT FOR UPDATE`). Database-level constraints are also a mandatory safety net.

**Warning signs:**
Inventory counts don't match order counts; intermittent errors under load.

**Phase to address:**
Phase 3 (Order & Inventory Module).

---

### Pitfall 3: Insecure JWT Handling

**What goes wrong:**
Users can hijack sessions, forge tokens, or use expired tokens if not properly validated.

**Why it happens:**
Weak signing keys, missing expiration checks, or exposing sensitive data (like passwords) in the JWT payload.

**How to avoid:**
Use strong HS256/RS256 keys; strictly validate `exp`, `iat`, and `iss` claims; use secure HTTPS for all token transmissions.

**Warning signs:**
Tokens work after they should have expired; tokens with no signature are accepted.

**Phase to address:**
Phase 1 (Security & Auth setup).

---

### Pitfall 4: Broken Object Level Authorization (BOLA/IDOR)

**What goes wrong:**
A user can view or modify another user's order by simply changing the ID in the URL/payload.

**Why it happens:**
The backend checks if the user is logged in, but not if they own the resource they are requesting.

**How to avoid:**
Always include a check in the service layer: `if (!order.getUserId().equals(currentUser.getId())) throw ForbiddenException`.

**Warning signs:**
Accessing `/api/orders/500` works for User A even if the order belongs to User B.

**Phase to address:**
Phase 2 (Service Layer implementation).

## Technical Debt Patterns

| Shortcut | Immediate Benefit | Long-term Cost | When Acceptable |
|----------|-------------------|----------------|-----------------|
| Skipping DTOs | Less boilerplate. | Security leaks; tight coupling of API to DB. | Never for public APIs. |
| Hardcoding Secrets| Fast setup. | Major security risk; deployment friction. | Local development only (via .env). |
| Lazy Testing | Faster feature delivery.| High regression risk; hard to refactor. | Small experiments/POCs. |

## Integration Gotchas

| Integration | Common Mistake | Correct Approach |
|-------------|----------------|------------------|
| PostgreSQL | Missing indexes on foreign keys. | Always index columns used in JOINs or WHERE clauses. |
| JWT | Using `java.util.Date` for timestamps. | Use `java.time.Instant` for better precision and API consistency. |
| MapStruct | Mapping nested objects without a sub-mapper. | Define explicit mapping methods for nested complex types. |

## Performance Traps

| Trap | Symptoms | Prevention | When It Breaks |
|------|----------|------------|----------------|
| Large Result Sets | OutOfMemoryError; slow API response. | Use Spring Data `Pageable` for all list endpoints. | > 1000 records. |
| Synchronous Emails| Slow order finalization. | Use `@Async` or a message queue for external notifications. | Under moderate load. |

## Recovery Strategies

| Pitfall | Recovery Cost | Recovery Steps |
|---------|---------------|----------------|
| N+1 Problem | MEDIUM | Identify with logs; update Repository queries to use FETCH. |
| Inventory Mismatch | HIGH | Manual audit; update stock; refund oversold customers. |
| BOLA Leak | CRITICAL | Patch logic immediately; audit logs for data breach assessment. |

## Sources

- OWASP Top 10 for API Security.
- Hibernate Performance Tuning guides.
- Personal experience with Spring Boot scaling.

---
*Pitfalls research for: E-commerce Backend*
*Researched: 2026-04-05*
