# Phase 1: Foundation & Authentication - Research

**Researched:** 2025-04-04
**Domain:** Spring Boot 3.4, Spring Security 6.4, JWT, Java 21, Flyway, PostgreSQL 16
**Confidence:** HIGH

## Summary

This research establishes the technical foundation for the `shop-back` project using Spring Boot 3.4 and Java 21. Key findings include the mechanism for enabling Virtual Threads, the transition to Lambda DSL in Spring Security 6.4, and the modularization of Flyway for PostgreSQL 16 support. The authentication system will utilize JJWT 0.13.0 for stateless token management with a database-backed Refresh Token rotation strategy.

**Primary recommendation:** Use `spring.threads.virtual.enabled=true` for high-throughput I/O and implement a custom `OncePerRequestFilter` for JWT validation to meet the specific requirements for Access (30s) and Refresh (DB stored) tokens.

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions
- **Build Tool**: Maven
- **Architecture**: Traditional Layered Architecture (`controller`, `service`, `repository`, `entity`, `dto`)
- **Java Version**: 21 (Virtual Threads enabled)
- **Framework**: Spring Boot 3.4+
- **Database**: PostgreSQL 16
- **Migration Tool**: Flyway
- **Auth Strategy**:
    - **Access Token**: 30 seconds
    - **Refresh Token**: Stored in Database
    - **Security**: Spring Security 6.x Lambda DSL + JWT

### the agent's Discretion
- Spring Security 6.4+ implementation details (Lambda DSL).
- Java 21 Virtual Threads configuration.
- JWT Library selection (Recommended: JJWT).
- Common Error Response DTO implementation.

### Deferred Ideas (OUT OF SCOPE)
- None mentioned for Phase 1.
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| FOUND-01 | Virtual Threads activation | Verified `spring.threads.virtual.enabled=true` property. |
| AUTH-01 | JWT Filter Implementation | Verified `OncePerRequestFilter` pattern with Spring Security 6.4. |
| AUTH-02 | Refresh Token DB Storage | Identified `RefreshToken` entity and service logic for rotation. |
| DB-01 | Flyway + PostgreSQL 16 | Verified modular dependency requirements for PostgreSQL support. |
| ARCH-01 | Maven Layered Structure | Defined standard package layout and dependency management. |
| RESP-01 | Common Error Response | Defined custom DTO and `@RestControllerAdvice` mapping. |
</phase_requirements>

## Standard Stack

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Spring Boot | 3.4.3 | Application Framework | Latest stable, Jakarta EE 10 support. |
| Spring Security | 6.4.x | Authentication/Authorization | Native Spring integration, Lambda DSL. |
| JJWT | 0.13.0 | JWT Creation/Validation | Industry standard for Java JWT. |
| Flyway | 10.x | Database Migration | Reliable schema versioning. |

### Supporting
| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| MapStruct | 1.6.3 | DTO Mapping | Performance-first object mapping. |
| Lombok | 1.18.36 | Boilerplate Reduction | Standard for clean Java entities/DTOs. |
| PostgreSQL | 42.7.x | JDBC Driver | High-performance PostgreSQL driver. |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| JJWT | Spring OAuth2 Resource Server | User specifically requested custom Access/Refresh flow with DB storage. |
| MapStruct | ModelMapper | MapStruct is compile-time (faster, type-safe). |

**Installation (Maven):**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.13.0</version>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
</dependency>
```

## Architecture Patterns

### Recommended Project Structure
```
src/main/java/com/shop/
├── config/             # Security, JPA, Virtual Threads config
├── controller/         # REST Controllers
├── service/            # Business Logic & RefreshToken Service
├── repository/         # Spring Data JPA Repositories
├── entity/             # JPA Entities (User, RefreshToken)
├── dto/                # Request/Response DTOs
├── exception/          # Global Exception Handler & Custom Exceptions
├── security/           # JWT Filter, Token Provider
└── util/               # Constants, Common Helpers
```

### Pattern 1: JWT Filter (Lambda DSL)
**What:** Modern Spring Security configuration using function-based DSL.
**When to use:** All Spring Security 6.x projects.
**Example:**
```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http
        .csrf(csrf -> csrf.disable())
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/**").permitAll()
            .anyRequest().authenticated()
        )
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
}
```

### Anti-Patterns to Avoid
- **Using `.and()`:** Deprecated in Spring Security 6.x; use lambdas instead.
- **Direct Entity Exposure:** Always map Entities to DTOs for API responses to prevent accidental data leakage (e.g., password hashes).

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| JWT Parsing | Custom base64 logic | JJWT | Handles crypto, padding, and validation edge cases securely. |
| DTO Mapping | Manual Getters/Setters | MapStruct | Reduces boilerplate and prevents "forgotten field" bugs. |
| Thread Pools | Custom ExecutorService | Virtual Threads | `spring.threads.virtual.enabled` optimizes the entire stack (Tomcat, JPA, etc.) automatically. |

## Common Pitfalls

### Pitfall 1: Flyway PostgreSQL Support
**What goes wrong:** `FlywayException: Unsupported Database: PostgreSQL 16`.
**Why it happens:** Flyway 10+ requires a separate database-specific dependency.
**How to avoid:** Add `flyway-database-postgresql` to dependencies.

### Pitfall 2: Virtual Thread Pinning
**What goes wrong:** Scalability drops when using Virtual Threads.
**Why it happens:** `synchronized` blocks or native calls can "pin" a virtual thread to a platform thread.
**How to avoid:** Prefer `ReentrantLock` over `synchronized` where performance is critical. Fortunately, Spring Boot 3.4 internals are largely optimized for this.

## Code Examples

### Global Error Response Implementation
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException e) {
        return ResponseEntity
            .status(e.getStatus())
            .body(new ErrorResponse(
                e.getCode(),
                e.getMessage(),
                LocalDateTime.now().toString(),
                null
            ));
    }
}
```

### Virtual Threads Configuration
```properties
# application.properties
spring.threads.virtual.enabled=true
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| `antMatchers()` | `requestMatchers()` | Spring Security 6.0 | Better security, type-safe matching. |
| Platform Threads | Virtual Threads | Java 21 / SB 3.2 | Dramatic increase in concurrent I/O capacity. |
| `and()` DSL | Lambda DSL | Spring Security 6.0 | Improved readability and configuration safety. |

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| Java 21 | Virtual Threads | ✓ | 21.0.x | — |
| PostgreSQL 16 | Data Store | ✓ | 16.x | — |
| Maven | Build | ✓ | 3.9.x | — |

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Framework | JUnit 5 + Mockito + AssertJ |
| Config file | `src/test/resources/application-test.yml` |
| Quick run command | `mvn test -Dgroups=unit` |
| Full suite command | `mvn test` |

### Phase Requirements → Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| AUTH-01 | JWT Token validation | Unit | `mvn test -Dtest=JwtServiceTest` | ❌ Wave 0 |
| AUTH-02 | Refresh Token rotation | Integration | `mvn test -Dtest=AuthIntegrationTest` | ❌ Wave 0 |

## Sources

### Primary (HIGH confidence)
- Spring Boot 3.4 Release Notes - Virtual Threads & Undertow support.
- Spring Security 6.4 Documentation - Lambda DSL & Migration guides.
- JJWT Official Documentation - Version 0.13.0 API changes.
- Flyway Documentation - Database modularization (v10+).

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH - Current stable versions verified.
- Architecture: HIGH - Standard layered pattern.
- Pitfalls: HIGH - Documented issues with Flyway/PostgreSQL 16.

**Research date:** 2025-04-04
**Valid until:** 2025-05-04 (Stable tech stack)
