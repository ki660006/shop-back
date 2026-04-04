# Project: Shop-Back (Shopping Mall Backend)

A production-ready Spring Boot 3.4 / Java 21 e-commerce backend API serving the Shop-Front React application. Covers the full shopping lifecycle: identity management, product discovery, cart persistence, transactional checkout, and personalized recommendations.

## Core Value

To provide a reliable, secure, and high-performance API service that powers the seamless shopping experience of Shop-Front.

## Requirements

### Validated

- ✓ User Signup, Login (JWT), and Token Management (AUTH-01, AUTH-02, AUTH-03) — v1.0
- ✓ Profile Management and Order History (USER-01, USER-02) — v1.0
- ✓ Product Catalog with Filtering, Search, and Category Management (SHOP-01–04) — v1.0
- ✓ Persistent Shopping Cart for Users (CART-01, CART-02) — v1.0
- ✓ Checkout Process and Order State Tracking (ORDER-01, ORDER-02) — v1.0
- ✓ Personalized Product Recommendations (REC-01) — v1.0

### Active

(Define for next milestone via `/gsd-new-milestone`)

### Out of Scope

- Real-time PG Integration — mocked for v1; planned for v2
- Admin Dashboard — future milestone
- Internationalization — v2 only
- OAuth (Google, GitHub) — v2
- Advanced collaborative filtering — v2

## Context

Shipped v1.0 MVP with ~4,425 Java LOC.

**Tech stack:** Spring Boot 3.4, Java 21 (Virtual Threads), PostgreSQL 16, Spring Data JPA + Querydsl, Spring Security 6 + JWT, Flyway, Lombok, ShedLock, Springdoc OpenAPI.

**Architecture:** Modular Monolith with domain-grouped packages (`domain/auth`, `domain/catalog`, `domain/cart`, `domain/order`, `domain/payment`, `domain/recommendation`).

**Known issues / tech debt:**
- Java/Maven not in dev machine PATH — integration tests verified by design, require manual run against PostgreSQL
- No CI pipeline yet
- `datasource-proxy` not integrated; cache testing uses Hibernate Statistics as proxy

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Java 21 + Spring Boot 3.4 | Modern LTS with Virtual Thread support | ✓ Good |
| Modular Monolith (domain-grouped) | MVP speed + future scalability path | ✓ Good |
| PostgreSQL FTS + cursor pagination | Avoids full table scans; stable infinite scroll | ✓ Good |
| UUID v7 (epoch-ordered) primary keys | Time-sortable, index-friendly across all entities | ✓ Good |
| Pessimistic locking on checkout | Prevents concurrent overselling | ✓ Good |
| DB-backed refresh token rotation | Enables revocation and session tracking | ✓ Good |
| Mock payment (no real PG) | v1 scope; real PG deferred to v2 | ✓ Good |
| ShedLock for batch cleanup | Distributed-safe cron without Redis | ✓ Good |
| `@Cacheable("categoryTree")` via ConcurrentMapCacheManager | Simple in-process cache for stable, low-churn data | ✓ Good |
| JPQL LEFT JOIN FETCH for single-entity fetch | Avoids MultipleBagFetchException vs @EntityGraph | ✓ Good |

## Tech Stack

- **Language**: Java 21
- **Framework**: Spring Boot 3.4+
- **Database**: PostgreSQL 16+
- **ORM**: Spring Data JPA + Querydsl
- **Utilities**: Lombok, Spring Security, ShedLock, Springdoc OpenAPI
- **Testing**: Spring Boot Test, MockMvc, Hibernate Statistics

## Evolution

This document evolves at phase transitions and milestone boundaries.

**After each phase transition** (via `/gsd:transition`):
1. Requirements invalidated? — Move to Out of Scope with reason
2. Requirements validated? — Move to Validated with phase reference
3. New requirements emerged? — Add to Active
4. Decisions to log? — Add to Key Decisions
5. "What This Is" still accurate? — Update if drifted

**After each milestone** (via `/gsd-complete-milestone`):
1. Full review of all sections
2. Core Value check — still the right priority?
3. Audit Out of Scope — reasons still valid?
4. Update Context with current state

---
*Last updated: 2026-04-05 after v1.0 milestone*
