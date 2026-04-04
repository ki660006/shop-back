# Project Research Summary

**Project:** shop-back
**Domain:** E-commerce Backend (Java Spring Boot)
**Researched:** 2026-04-05
**Confidence:** HIGH

## Executive Summary

The `shop-back` project is a high-performance e-commerce backend built with the 2026 standard Java/Spring Boot stack. It serves as the primary data and logic layer for the `shop-front` application, transitioning from mocked services to a robust, persistent production-ready API.

The recommended approach is a **Modular Monolith** architecture using **Java 21**, **Spring Boot 3.4+**, and **PostgreSQL**. This setup balances rapid development for the MVP with a clear path toward future microservices scalability. Key focus areas include secure JWT-based authentication, type-safe data access via Querydsl, and preventing common pitfalls such as the N+1 query problem and race conditions in inventory management.

## Key Findings

### Recommended Stack

A modern, high-performance stack centered on Java 21's Virtual Threads and Spring Boot 3.4's improved developer experience.

**Core technologies:**
- **Java 21 (LTS):** Modern language features and performance improvements via Project Loom.
- **Spring Boot 3.4+:** Industry-standard framework for building robust, modular backend services.
- **PostgreSQL 16+:** Relational data storage with strong support for JSON and concurrent transactions.
- **Hibernate & Querydsl:** Efficient ORM with type-safe, dynamic query building.

### Expected Features

Alignment with `shop-front` requirements while ensuring backend integrity.

**Must have (table stakes):**
- **JWT Authentication:** Secure, stateless login/registration.
- **Product Catalog:** Feature-rich browsing and detail management.
- **Shopping Cart & Orders:** Reliable cart persistence and complex order state management.

**Should have (competitive):**
- **Personalized Recommendations (REC):** Backend logic to serve relevant items based on user history.
- **Advanced Search:** Decoupled search indexing for faster, more relevant results.

**Defer (v2+):**
- **Multi-vendor Support:** Essential only after the core B2C flow is validated.
- **Global Scale Features:** CDN, geo-replication, etc.

### Architecture Approach

The project will follow a **Modular Monolith** pattern, organizing code by domain (e.g., `modules/catalog`, `modules/order`). This ensures high cohesion within features and loose coupling between them, facilitating both maintenance and potential future extraction into microservices.

**Major components:**
1. **API Security Filter:** Handles JWT validation and CORS globally.
2. **Domain Modules:** Encapsulated business logic for each functional area.
3. **DTO/Mapping Layer:** Strict separation between database entities and public API contracts.

### Critical Pitfalls

Top risks that must be managed during implementation:

1. **Hibernate N+1 Problem:** Mitigate early with proper `JOIN FETCH` and Entity Graph usage.
2. **Inventory Race Conditions:** Use `@Version` (optimistic locking) to prevent overselling.
3. **Broken Object Level Authorization (BOLA):** Enforce strict ownership checks on all resource requests.

## Implications for Roadmap

### Phase 1: Foundation & Security
**Rationale:** Establishing a secure, well-structured base is critical before building functional modules.
**Delivers:** Core structure, JWT security, user registration, and database migrations.
**Addresses:** AUTH, User Profiles.
**Avoids:** Insecure JWT Handling.

### Phase 2: Catalog & Search
**Rationale:** The product catalog is the foundation of the e-commerce experience.
**Delivers:** REST APIs for product browsing, filtering, and keyword search.
**Addresses:** SHOP (Catalog/Search).
**Avoids:** N+1 Query Problem (via early repository optimization).

### Phase 3: Cart & Order Management
**Rationale:** Completing the transactional loop of the e-commerce journey.
**Delivers:** Persistence for shopping carts and complex order placement logic.
**Addresses:** CART, ORDER.
**Avoids:** Inventory Race Conditions (via locking strategies).

### Phase 4: Recommendations & Enhancements
**Rationale:** Adding differentiation once the core shopping flow is stable.
**Delivers:** Personalization engine and advanced search features.
**Addresses:** REC, Performance tuning.

### Phase Ordering Rationale

- **Foundation first:** Security and database setup must precede functional modules to ensure consistent data integrity and safety.
- **Catalog before Order:** Orders cannot exist without a reliable product catalog to reference.
- **Enhancements last:** Features like recommendations are built on top of the data collected during the core shopping journey.

## Confidence Assessment

| Area | Confidence | Notes |
|------|------------|-------|
| Stack | HIGH | Based on 2026 industry standards and Spring's stable roadmap. |
| Features | HIGH | Directly maps to `shop-front` requirements and common e-commerce patterns. |
| Architecture | HIGH | Modular Monolith is the proven best-practice for starting such projects. |
| Pitfalls | HIGH | Well-documented issues in the Spring/Hibernate ecosystem. |

**Overall confidence:** HIGH

### Gaps to Address

- **Payment Integration:** Deeper research into specific PG (Payment Gateway) APIs will be needed in Phase 3.
- **Search Engine Scale:** Deciding when to transition from PostgreSQL search to Elasticsearch based on initial catalog size.

## Sources

### Primary (HIGH confidence)
- Spring Boot & Hibernate Official Documentation.
- OWASP Top 10 API Security Project.
- Modular Monolith patterns (Spring Modulith).

---
*Research completed: 2026-04-05*
*Ready for roadmap: yes*
