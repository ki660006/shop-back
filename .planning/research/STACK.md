# Stack Research

**Domain:** E-commerce Backend (Java Spring Boot)
**Researched:** 2026-04-05
**Confidence:** HIGH

## Recommended Stack

### Core Technologies

| Technology | Version | Purpose | Why Recommended |
|------------|---------|---------|-----------------|
| Java | 21 (LTS) | Programming Language | Modern features (Virtual Threads, Records, Pattern Matching), long-term support. |
| Spring Boot | 3.4+ | Core Framework | Industry standard for Java microservices/monoliths; simplified configuration and robust ecosystem. |
| Spring Security | 6.4+ | Security & Auth | Comprehensive security support; integrates seamlessly with JWT and OAuth2. |
| PostgreSQL | 16+ | Primary Database | Robust, ACID-compliant relational database; excellent JSONB support for semi-structured data. |
| Hibernate (JPA) | 6.6+ | ORM | Default JPA provider for Spring; powerful data mapping and caching. |

### Supporting Libraries

| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| MapStruct | 1.6+ | Bean Mapping | Compile-time mapper for DTO-to-Entity conversion; faster than reflection-based mappers. |
| Querydsl | 6.0+ | Type-safe Queries | Building dynamic, type-safe SQL-like queries in Java; avoids string-based JPQL. |
| Nimbus JOSE+JWT | 9.x | JWT Handling | High-performance, mature library for generating and verifying JSON Web Tokens. |
| Lombok | 1.18+ | Boilerplate Reduction | Automatic generation of getters, setters, and constructors via annotations. |
| SpringDoc OpenAPI| 2.x | API Documentation | Automatically generates Swagger/OpenAPI UI from Spring Boot controllers. |

### Development Tools

| Tool | Purpose | Notes |
|------|---------|-------|
| Maven / Gradle | Build Automation | Gradle is preferred for faster builds; Maven for stability and widespread usage. |
| Docker | Containerization | Standardizes development and production environments. |
| Testcontainers | Integration Testing | Spin up real PostgreSQL/Redis instances during unit/integration tests. |

## Installation

```bash
# Core (Example using Spring Initializr or Maven dependencies)
# Add dependencies to pom.xml or build.gradle

# Key dependencies for Maven:
# spring-boot-starter-web
# spring-boot-starter-data-jpa
# spring-boot-starter-security
# spring-boot-starter-validation
# postgresql (runtime)
# querydsl-jpa
# mapstruct
```

## Alternatives Considered

| Recommended | Alternative | When to Use Alternative |
|-------------|-------------|-------------------------|
| PostgreSQL | MySQL | If specific legacy compatibility or specific cloud-managed features are required. |
| Hibernate | MyBatis | If fine-grained control over raw SQL is prioritized over ORM abstractions. |
| Querydsl | JOOQ | If you prefer a more SQL-centric approach and don't mind the code generation overhead. |

## What NOT to Use

| Avoid | Why | Use Instead |
|-------|-----|-------------|
| Java 8/11 | End of life / Lack of modern performance features (Virtual Threads). | Java 21 (LTS) |
| ModelMapper | Uses reflection; slow and difficult to debug compared to MapStruct. | MapStruct |
| Raw JDBC | Error-prone and verbose for complex domain models. | Spring Data JPA / Querydsl |

## Stack Patterns by Variant

**If High Concurrency is expected:**
- Use Spring WebFlux instead of Spring MVC.
- Because it's non-blocking and handles more connections with fewer threads (though Virtual Threads in Java 21 make MVC competitive again).

**If Search is a primary feature:**
- Use Elasticsearch / OpenSearch.
- Because relational databases struggle with complex full-text search and faceted navigation at scale.

## Version Compatibility

| Package A | Compatible With | Notes |
|-----------|-----------------|-------|
| Spring Boot 3.4 | Spring Security 6.4 | Standard alignment in Spring Boot 3.4 BOM. |
| Java 21 | Spring Boot 3.x | Spring Boot 3+ requires Java 17 minimum; 21 is fully supported. |

## Sources

- Spring Boot Official Documentation
- Java 21 Release Notes
- Hibernate 6.x Migration Guide
- Community best practices for 2026 e-commerce architectures.

---
*Stack research for: E-commerce Backend*
*Researched: 2026-04-05*
