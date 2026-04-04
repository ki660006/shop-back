# CLAUDE.md for shop-back

## Project: Shop-Back
E-commerce backend using Java 21 and Spring Boot 3.4+.

## Build & Run Commands
- Build: `./mvnw clean install`
- Run: `./mvnw spring-boot:run`
- Test: `./mvnw test`
- Lint: `./mvnw spotless:check` (if configured) or standard IDE formatting.

## Code Style & Standards
- **Java Version**: 21 (LTS) - use Virtual Threads (`spring.threads.virtual.enabled=true`).
- **Framework**: Spring Boot 3.4+.
- **Packaging**: `com.shop.back` (base package).
- **Architecture**: Modular Monolith. Organize by domain (e.g., `modules.auth`, `modules.catalog`).
- **Persistence**: Spring Data JPA with PostgreSQL.
- **Security**: Spring Security + JWT.
- **API Style**: RESTful with JSON. Use DTOs for request/response bodies.
- **Naming**: CamelCase for Java, kebab-case for URLs, snake_case for DB columns.
- **Error Handling**: Centralized `@RestControllerAdvice`.
- **Lombok**: Preferred for reducing boilerplate (Getters, Setters, Slf4j).

## Important Context
- This is the backend for `shop-front`.
- Focus on performance (avoid N+1) and security (JWT, BOLA).
- Follow the `.planning/ROADMAP.md` for phase-based development.
