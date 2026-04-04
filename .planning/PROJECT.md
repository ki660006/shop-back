# Project: Shop-Back (Shopping Mall Backend)
A robust and scalable backend for the Shop-Front project, providing RESTful APIs for product management, user authentication, and order processing.

## Core Value
To provide a reliable, secure, and high-performance API service that powers the seamless shopping experience of Shop-Front.

## Requirements

### Validated
(None yet - ship to validate)

### Active
- [ ] [AUTH] User Signup, Login (JWT), and Token Management
- [ ] [USER] Profile Management and Order History
- [ ] [SHOP] Product Catalog with Filtering, Search, and Category Management
- [ ] [CART] Persistent Shopping Cart for Users
- [ ] [ORDER] Checkout Process and Order State Tracking
- [ ] [REC] Personalized Product Recommendations

### Out of Scope
- [Real-time PG Integration] - Mocked for v1
- [Admin Dashboard] - Future milestone
- [Internationalization] - v2 only

## Key Decisions
| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Java Spring Boot | Industry standard for robust backends | Pending |
| PostgreSQL | Powerful relational database for complex queries | Pending |
| JPA / Lombok | Boosts development speed and maintainability | Pending |
| JWT Authentication | Secure, stateless authentication for React frontend | Pending |

## Tech Stack
- **Language**: Java 21
- **Framework**: Spring Boot 3.4+
- **Database**: PostgreSQL 16+
- **ORM**: Spring Data JPA
- **Utilities**: Lombok, Spring Security

## Evolution
This document evolves at phase transitions and milestone boundaries.

**After each phase transition** (via `/gsd:transition`):
1. Requirements invalidated? - Move to Out of Scope with reason
2. Requirements validated? - Move to Validated with phase reference
3. New requirements emerged? - Add to Active
4. Decisions to log? - Add to Key Decisions
5. "What This Is" still accurate? - Update if drifted

**After each milestone** (via `/gsd:complete-milestone`):
1. Full review of all sections
2. Core Value check - still the right priority?
3. Audit Out of Scope - reasons still valid?
4. Update Context with current state

---
*Last updated: 2026-04-04 after initialization*
