# Phase 1 Context: Foundation & Authentication

## 결정된 사항 (Decisions)

### 1. 기술 스택 및 구조
- **Build Tool**: Maven
- **Architecture**: Traditional Layered Architecture (`controller`, `service`, `repository`, `entity`, `dto`)
- **Java Version**: 21 (Virtual Threads 활용 고려)
- **Framework**: Spring Boot 3.4+

### 2. 사용자 및 인증 (Auth)
- **User Fields**: email(username), password, name, nickname, phone, address, role
- **JWT Strategy**:
    - **Access Token**: 30 seconds (Short-lived for testing refresh flow)
    - **Refresh Token**: Implemented (Stored in Database for Phase 1)
- **Security**: Spring Security 6.x 기반 JWT 필터 구현
- **Secret Management**: Environment variables (e.g., `JWT_SECRET`) 및 local profiles 활용

### 3. API & 데이터 표준
- **JSON Case**: `camelCase`
- **Error Response Structure**:
  ```json
  {
    "code": "ERROR_CODE",
    "message": "Human readable message",
    "timestamp": "ISO-8601",
    "details": "Additional error context or null"
  }
  ```

### 4. 데이터베이스 및 마이그레이션
- **Database**: PostgreSQL 16
- **Migration Tool**: Flyway (트렌드 반영 및 형상 관리 자동화)
- **ERD Scope (Phase 1)**: `users`, `refresh_tokens`

## 조사 및 확인이 필요한 사항 (To Investigate)
- Spring Security 6.4+ 에서의 최신 Lambda DSL 설정 방식 확인
- Java 21 Virtual Threads를 Spring Boot에서 활성화하는 설정 (`spring.threads.virtual.enabled`)
- JWT 라이브러리 선정 (jjwt vs spring-security-jwt)

## 제약 사항 (Constraints)
- 프로젝트 초기 상태로, 모든 DB 스키마는 Flyway를 통해 생성되어야 함.
- Access Token 만료 시간은 반드시 30초로 설정하여 Refresh flow를 검증 가능하게 할 것.
