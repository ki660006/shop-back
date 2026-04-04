# Roadmap: Shop-Back (Java Spring Boot E-commerce)

## Phases
- [ ] **Phase 1: Foundation & Authentication** - Secure backend core with user identity management.
- [ ] **Phase 2: Product Catalog & Discovery** - Search, filtering, and detailed product views.
- [ ] **Phase 3: Shopping Cart Persistence** - Managing items across user sessions.
- [ ] **Phase 4: Checkout & Order Management** - Transactional flow from cart to history.
- [ ] **Phase 5: Personalization & Recommendations** - Heuristic-based suggestions and performance polish.

## Phase Details

### Phase 1: Foundation & Authentication
**Goal**: Establish a secure backend foundation with identity management.
**Depends on**: Nothing
**Requirements**: AUTH-01, AUTH-02, AUTH-03, USER-02
**Success Criteria** (what must be TRUE):
  1. User can successfully register an account via API POST request.
  2. User can log in and receive a valid JWT token.
  3. API requests without a valid token return 401 Unauthorized.
  4. User can update their own profile information via a secured endpoint.
**Plans**: 3 plans
- [ ] 01-01-PLAN.md — Project scaffolding, database setup, and global error handling.
- [ ] 01-02-PLAN.md — Spring Security configuration and core signup/login with JWT.
- [ ] 01-03-PLAN.md — User profile management and database-backed refresh token rotation.

### Phase 2: Product Catalog & Discovery
**Goal**: Implement a performant and searchable product catalog.
**Depends on**: Phase 1
**Requirements**: SHOP-01, SHOP-02, SHOP-03, SHOP-04
**Success Criteria** (what must be TRUE):
  1. User can retrieve a list of product categories.
  2. User can search for products using keywords and filter by price range and category.
  3. User can fetch full details of a specific product.
  4. Product reviews and Q&A are visible in product detail responses (mock data allowed for v1 content).
**Plans**: TBD

### Phase 3: Shopping Cart Persistence
**Goal**: Enable persistent cart state across browser sessions.
**Depends on**: Phase 1, Phase 2
**Requirements**: CART-01, CART-02
**Success Criteria** (what must be TRUE):
  1. User can add products to their cart and see them persist after logging out and back in.
  2. User can modify item quantities and remove items from the cart.
  3. Cart calculations (subtotals, totals) are correctly handled on the server.
**Plans**: TBD

### Phase 4: Checkout & Order Management
**Goal**: Complete the transactional loop for purchases and history.
**Depends on**: Phase 3
**Requirements**: ORDER-01, ORDER-02, USER-01
**Success Criteria** (what must be TRUE):
  1. User can successfully place an order using their current cart (checkout).
  2. User can retrieve their entire order history.
  3. User can fetch the full details of a specific past order.
  4. Inventory stock is decremented (or checked) during order placement.
**Plans**: TBD

### Phase 5: Personalization & Polish
**Goal**: Enhance user experience with recommendations and performance refinements.
**Depends on**: Phase 4
**Requirements**: REC-01
**Success Criteria** (what must be TRUE):
  1. User sees a "Recommended for You" section based on their recent purchases or viewed categories.
  2. Major database queries are optimized to prevent N+1 issues (verified by query logs).
  3. All API documentation (Swagger/OpenAPI) is complete and accurate.
**Plans**: TBD

## Progress Table

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 1. Foundation & Authentication | 0/3 | In Progress | - |
| 2. Product Catalog & Discovery | 0/1 | Not started | - |
| 3. Shopping Cart Persistence | 0/1 | Not started | - |
| 4. Checkout & Order Management | 0/1 | Not started | - |
| 5. Personalization & Polish | 0/1 | Not started | - |
