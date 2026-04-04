# Phase 1, Wave 2: Foundation & Authentication Summary

## Objective
Implement core authentication using Spring Security 6.x and JWT.

## Tasks Completed
1. **Security Configuration & JWT Provider**:
   * Created `JwtTokenProvider.java` using JJWT (Access Token: 30s).
   * Created `SecurityConfig.java` (Spring Security 6.4+ Lambda DSL).
   * Implemented `CustomUserDetails` and `CustomUserDetailsService` for authentication.
2. **User Registration & Login (AUTH-01, AUTH-02)**:
   * Created `User.java` (Entity) and `UserRepository.java`.
   * Created `AuthService.java` for signup (password hashing) and login logic.
   * Created `AuthController.java` with `/api/auth/signup` and `/api/auth/login` endpoints.
   * Created `SignupRequest`, `LoginRequest`, and `AuthResponse` DTOs.
3. **JWT Authentication Filter & Integration**:
   * Created `JwtAuthenticationFilter.java` (OncePerRequestFilter).
   * Registered the filter in `SecurityConfig.java`.

## Pending / Interrupted Verification
- Automated verification was skipped because Java and Maven CLI tools are missing from the environment's `PATH`.

## Next Steps
Proceed to Wave 3 to implement user profile management and database-backed refresh token rotation.
