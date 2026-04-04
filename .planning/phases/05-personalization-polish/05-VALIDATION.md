---
phase: 5
slug: personalization-polish
status: draft
nyquist_compliant: false
wave_0_complete: false
created: 2026-04-05
---

# Phase 5 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit 5 + Spring Boot Test |
| **Config file** | `pom.xml` |
| **Quick run command** | `mvn test -Dtest=*IntegrationTest` |
| **Full suite command** | `mvn test` |
| **Estimated runtime** | ~90 seconds |

---

## Sampling Rate

- **After every task commit:** Run `mvn test -Dtest=*IntegrationTest`
- **After every plan wave:** Run `mvn test`
- **Before `/gsd-verify-work`:** Full suite must be green
- **Max feedback latency:** 90 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 05-01-01 | 01 | 1 | REC-01 | — | N/A | build | `mvn dependency:resolve` | ✅ | ⬜ pending |
| 05-01-02 | 01 | 1 | REC-01 | — | N/A | build | `mvn clean compile` | ❌ W0 | ⬜ pending |
| 05-01-03 | 01 | 1 | REC-01 | — | Swagger paths require no auth | build | `mvn clean compile` | ✅ | ⬜ pending |
| 05-02-01 | 02 | 2 | REC-01 | — | Endpoints require authentication | build | `mvn clean compile` | ❌ W0 | ⬜ pending |
| 05-02-02 | 02 | 2 | REC-01 | — | N/A | build | `mvn clean compile` | ✅ | ⬜ pending |
| 05-02-03 | 02 | 2 | REC-01 | — | N/A | build | `mvn clean compile` | ❌ W0 | ⬜ pending |
| 05-03-01 | 03 | 3 | REC-01 | — | N/A | build | `mvn clean compile` | ✅ | ⬜ pending |
| 05-03-02 | 03 | 3 | REC-01 | — | N/A | integration | `mvn test -Dtest=RecommendationIntegrationTest` | ❌ W0 | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

- [ ] `src/test/java/com/shop/RecommendationIntegrationTest.java` — stubs for REC-01
- [ ] `pom.xml` — `springdoc-openapi-starter-webmvc-ui` dependency added
- [ ] `pom.xml` — `hypersistence-utils-hibernate-63` test dependency added for SQLStatementCountValidator

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Swagger UI renders with JWT lock icon | REC-01 | Browser-based UI interaction | Open `/swagger-ui.html`, verify lock icon appears on all endpoints |
| Recommendations match recent product views | REC-01 | Requires seed data + session state | Login, view 3 products, call `GET /api/recommendations`, verify response reflects recent categories |

---

## Validation Sign-Off

- [ ] All tasks have `<automated>` verify or Wave 0 dependencies
- [ ] Sampling continuity: no 3 consecutive tasks without automated verify
- [ ] Wave 0 covers all MISSING references
- [ ] No watch-mode flags
- [ ] Feedback latency < 90s
- [ ] `nyquist_compliant: true` set in frontmatter

**Approval:** pending
