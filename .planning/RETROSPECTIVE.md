# Project Retrospective

*A living document updated after each milestone. Lessons feed forward into future planning.*

## Milestone: v1.0 — MVP

**Shipped:** 2026-04-05
**Phases:** 5 | **Plans:** 15

### What Was Built

- Secure JWT auth with DB-backed refresh token rotation and virtual threads
- Searchable product catalog: PostgreSQL FTS, UUID v7 cursor pagination, hierarchical categories
- Persistent shopping cart with guest/member merge strategy and ShedLock cleanup
- Transactional checkout with pessimistic locking preventing concurrent overselling
- Hybrid recommendation engine, N+1 elimination via EntityGraph, Swagger/OpenAPI docs

### What Worked

- **Domain-grouped modular monolith** — clean separation made each phase independently executable with minimal cross-phase conflicts
- **UUID v7 as universal key strategy** — decided early, applied consistently, zero friction across all phases
- **Pessimistic locking design** — decided in Phase 3 planning, proved correct in Phase 4 concurrent checkout tests
- **ShedLock for distributed cron** — simple, no Redis dependency, reused across cart cleanup and view cleanup batches
- **Phase summaries with deviation logs** — Phase 5 Wave 3 summary captured 6 pre-existing bugs fixed during execution, providing clear audit trail

### What Was Inefficient

- **Traceability table never updated** — all 14 requirements stayed "Pending" throughout development; required manual audit at milestone close
- **Progress Table in ROADMAP.md not maintained** — phase statuses not updated after each wave
- **Missing SUMMARY.md files** — 02-01 and 03-02 had no summaries (code was done but not documented); required inference from later waves
- **Java/Maven PATH issue** — `mvn compile` unavailable in environment throughout; automated verification deferred to manual step for every phase
- **`datasource-proxy` not in pom.xml** — required switching to Hibernate Statistics mid-Phase 5 for cache testing

### Patterns Established

- **Wave-based execution** within each phase (foundation → core logic → lifecycle/tests) proved effective for incremental validation
- **Integration test per phase** pattern (`CartIntegrationTest`, `OrderIntegrationTest`, etc.) established consistent test coverage structure
- **Fix-and-log deviations in SUMMARY** — Phase 5 caught and fixed 6 pre-existing issues; the practice of logging them with commit SHAs is worth keeping
- **`@PrePersist` for UUID/timestamp initialization** — applied uniformly across all entities

### Key Lessons

1. **Update traceability table during execution, not at close** — add it to the per-phase completion checklist
2. **Write SUMMARY.md immediately after each wave** — don't skip even for straightforward waves
3. **Verify environment constraints in Phase 1** — Java/Maven PATH was discovered in Wave 1 but never resolved, creating friction in every subsequent phase
4. **Pre-check all test dependencies in pom.xml before writing tests** — `datasource-proxy` gap caused mid-wave replanning in Phase 5

### Cost Observations

- Sessions: ~1-2 sessions across 2 days
- Notable: All 5 phases executed in ~2 days; high velocity due to clear phase goals and minimal scope ambiguity

---

## Cross-Milestone Trends

| Metric | v1.0 |
|--------|------|
| Phases | 5 |
| Plans | 15 |
| LOC | ~4,425 Java |
| Timeline | 2 days |
| Req coverage | 100% (14/14) |
| Bugs fixed during execution | 6 (Phase 5) |
