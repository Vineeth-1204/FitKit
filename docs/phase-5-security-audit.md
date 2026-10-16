# FitKit Phase 5 — Security & Authorization Audit

## 1. Executive Summary

A comprehensive security audit of FitKit was conducted across the backend codebase (`com.fitkit.*`), REST controllers, domain repositories, Flyway migrations, and frontend assets (`index.html`, `app.js`).

FitKit possesses high-integrity nutrition calculation models, immutable nutrient snapshots, and reliable MySQL database persistence. However, **the application currently possesses no authentication layer, no user table, and no server-side authorization context**. Instead, identity is either client-asserted via an optional `userId` query parameter or defaulted to `"default-user"`. Any client can query or mutate another user's journal entries or dietary targets simply by specifying their identifier.

This audit documents the current security posture, lists all endpoints requiring protection, evaluates ownership and data-isolation risks, outlines database schema migration requirements, and provides a prioritized implementation plan for Phase 5.

---

## 2. Current Authentication & Authorization Status

### 2.1 Backend Dependencies & Security Context
- **Spring Security**: Not present in `pom.xml`. The application currently runs with standard `spring-boot-starter-web`.
- **User Account Management**: No user entity (`User.java`), repository (`UserRepository.java`), or database table (`users`) exists.
- **Identity Derivation**: Endpoints in `MealLogController` and `UserTargetController` receive `userId` as an unauthenticated `@RequestParam(required = false, defaultValue = "default-user") String userId`.
- **Password Storage**: No password encoding, hashing (BCrypt), or credential validation mechanisms are implemented.
- **Session Management**: Default Tomcat HTTP session is unmanaged by Spring Security; no CSRF token validation, session regeneration on login, or SameSite cookie policies are active.
- **CORS Configuration**: `@CrossOrigin(origins = "*")` is present on `FoodCatalogController`, `RecipeController`, `SystemStatsController`, `MealLogController`, and `UserTargetController`. While benign for public catalog reads, allowing wildcard CORS on state-changing user endpoints with credentials is an insecure design.

### 2.2 Frontend Context
- **Identity Representation**: Hardcoded in `app.js` as `state.userId = 'default-user'`.
- **UI State**: The application boots directly into the daily journal dashboard without authentication gates.
- **Request Helpers**: All requests to `/api/meals`, `/api/targets`, and `/api/meals/migrate` query strings pass `?userId=${encodeURIComponent(state.userId)}`.
- **Session Expiration / 401 Handling**: None. Unauthorized responses are not caught or redirected to a login view.

---

## 3. Endpoints Audit & Security Classification

### 3.1 Public Endpoints (Permit All)
These endpoints contain canonical nutritional data, reference recipes, and system metadata containing zero private user data:
- `GET /api/foods`: Canonical food catalog search.
- `GET /api/foods/{id}`: Detailed proximate composition and portions.
- `GET /api/foods/groups`: Food category names.
- `GET /api/foods/states`: Food state enumeration values.
- `GET /api/recipes`: Standardized recipe list.
- `GET /api/recipes/{id}`: Recipe details and ingredients.
- `POST /api/recipes/{id}/calculate`: Deterministic yield factor and serving simulator.
- `POST /api/recipes/custom-calculate`: Custom ingredient yield simulator.
- `GET /api/stats`: Catalog counts and verified entry statistics.
- Static assets: `/`, `/index.html`, `/css/**`, `/js/**`, `/favicon.ico`.

### 3.2 Authentication Endpoints (New — Permit All)
Endpoints required to manage user onboarding and session lifecycle:
- `POST /api/auth/register`: Create new user account with validated email and BCrypt-hashed password.
- `POST /api/auth/login`: Authenticate credentials, regenerate session ID, establish SecurityContext.
- `POST /api/auth/logout`: Invalidate HTTP session, clear authentication context and cookies.
- `GET /api/auth/me`: Return authenticated user's profile and status (or 401 if unauthenticated).

### 3.3 Protected User Endpoints (Authenticated & Scoped to Principal)
These endpoints access private user data and must derive user identity exclusively from the authenticated Spring Security `Principal`:
- `GET /api/targets`: Retrieve authenticated user's daily calorie and macronutrient targets.
- `PUT /api/targets`: Update authenticated user's daily targets.
- `GET /api/meals`: Retrieve authenticated user's daily meal log for a specific date.
- `POST /api/meals/entries`: Add a food or recipe entry to the authenticated user's meal log.
- `PUT /api/meals/entries/{id}`: Update portion/multiplier for an entry owned by the authenticated user.
- `DELETE /api/meals/entries/{id}`: Delete an entry owned by the authenticated user.
- `GET /api/meals/history`: Retrieve 7-day progress history for the authenticated user.
- `POST /api/meals/migrate`: Idempotently import legacy localStorage diary records into the authenticated user's account.

---

## 4. Identified Authorization & Data-Isolation Risks

1. **Insecure Direct Identity Selection (Critical)**:
   - *Vulnerability*: Clients supply `userId` via query parameters (e.g. `/api/meals?userId=admin` or `/api/targets?userId=user-42`).
   - *Impact*: Any user can inspect, overwrite, or delete any other user's meal logs and nutritional targets.
   - *Remediation*: Discard client-supplied `userId` completely. Bind all controller and service operations to `SecurityContextHolder.getContext().getAuthentication().getName()`.
2. **Insecure Direct Object Reference (IDOR) on Meal Entries (High)**:
   - *Vulnerability*: `PUT /api/meals/entries/{id}` and `DELETE /api/meals/entries/{id}` operate on numeric entry IDs.
   - *Impact*: If a malicious user discovers another user's entry ID, they could manipulate or delete records across accounts unless strict ownership checks are enforced.
   - *Remediation*: Verify that `entry.getMealLog().getUser().getId()` (or email) matches the authenticated principal before performing mutations. Return `404 Not Found` (to avoid leaking existence) or `403 Forbidden`.
3. **Wildcard CORS with Credentialed Requests (Moderate)**:
   - *Vulnerability*: `@CrossOrigin(origins = "*")` on mutating controllers.
   - *Impact*: Cross-site requests from untrusted origins could execute state-changing actions if browser sessions or credentials are used.
   - *Remediation*: Remove `@CrossOrigin(origins = "*")` from user-specific controllers. Configure Spring Security CORS to restrict access to same-origin or explicitly trusted application origins.
4. **Lack of CSRF Protection (Moderate)**:
   - *Vulnerability*: State-changing POST/PUT/DELETE endpoints without CSRF tokens.
   - *Impact*: Authenticated cookie sessions can be susceptible to Cross-Site Request Forgery.
   - *Remediation*: Configure Spring Security CSRF repository with `CookieCsrfTokenRepository.withHttpOnlyFalse()`, ensuring frontend JavaScript extracts the `XSRF-TOKEN` cookie and supplies the `X-XSRF-TOKEN` header on all modifying requests.
5. **Legacy Data Orphan Risk (`default-user`) (Low/Moderate)**:
   - *Vulnerability*: Existing records created during Phase 3 and 4 development have `user_id = 'default-user'`.
   - *Impact*: When authentication is enforced, `default-user` data could either be orphaned or exposed to an unauthenticated caller.
   - *Remediation*: Migration V5 will seed a canonical default user account (`demo@fitkit.local` / `default-user`) with known development credentials, preserving all historical development records while closing the anonymous bypass.

---

## 5. Existing Components Suitable for Reuse

1. **`MealLoggingService` & `MealLogRepository`**:
   - The query structures, nutrient snapshot calculations, and update/delete routines are mathematically sound and will be preserved. We only need to switch the `userId` parameter to the authenticated user's stable identifier (email or user ID).
2. **`UserTargetService` & `UserTargetRepository`**:
   - Reused directly by binding queries to the authenticated user's ID/email.
3. **Frontend Views, Modals & Styling (`index.html`, `styles.css`, `app.js`)**:
   - The approved editorial design system (warm ivory, charcoal, terracotta) will be retained.
   - The authentication UI (Login / Register view) will be crafted using the exact same typographic scale (`Fraunces` / `Inter`), color tokens, input borders, and button components.

---

## 6. Database Migration Requirements (`V5`)

### 6.1 `users` Table
```sql
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(150),
    role VARCHAR(50) NOT NULL DEFAULT 'ROLE_USER',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_users_email ON users(email);
```

### 6.2 Legacy Record Preservation
- Seed a standard default account for development and backward compatibility:
  - Email: `demo@fitkit.local`
  - Name: `Demo Athlete`
  - Password Hash: BCrypt hash of `FitKit2026!` (`$2a$10$...`)
- Update existing `user_targets` and `meal_logs` records where `user_id = 'default-user'` to ensure foreign-key consistency or preserve `user_id = 'demo@fitkit.local'`.

---

## 7. Prioritized Implementation Plan

1. **Step 1: Security Dependencies & Database Migration**:
   - Add `spring-boot-starter-security` and `spring-security-test` to `pom.xml`.
   - Create Flyway migration `V5__create_users_table_and_auth_schema.sql`.
   - Create `User` entity, `UserRepository`, and `UserRole` enum.
2. **Step 2: Security Configuration & Authentication Service**:
   - Configure `SecurityFilterChain`:
     - Public routes: static resources, `/api/auth/**`, `/api/foods/**`, `/api/recipes/**`, `/api/stats/**`.
     - Protected routes: `/api/meals/**`, `/api/targets/**`.
     - Session management: `SessionCreationPolicy.IF_REQUIRED`, session fixation protection (`changeSessionId`), HttpOnly session cookies.
     - CSRF protection: `CookieCsrfTokenRepository.withHttpOnlyFalse()`.
   - Implement `CustomUserDetailsService` loading users from `users` table.
   - Implement `AuthService` handling registration (BCrypt password encoding) and authentication.
   - Create `AuthController` (`/api/auth/register`, `/api/auth/login`, `/api/auth/logout`, `/api/auth/me`).
3. **Step 3: Server-Side Authorization Hardening**:
   - Refactor `MealLogController` and `UserTargetController` to extract the authenticated user identity via `Principal` / `Authentication.getName()`.
   - Remove client-provided `userId` parameters from API contracts.
   - Enforce ownership validation in `MealLoggingService` and `UserTargetService`.
4. **Step 4: Frontend Auth Integration & Editorial UI Flow**:
   - Add Login / Register view in `index.html` styled with the warm ivory editorial theme.
   - Update `app.js` with authentication status check (`/api/auth/me`), login/registration handlers, CSRF header attachment (`X-XSRF-TOKEN`), session error handling (401 redirect to login), and logout trigger.
   - Add user account status and logout button to the topbar/sidebar.
5. **Step 5: Automated Security & Isolation Testing**:
   - Write `SecurityAndAccountIsolationTest.java` verifying:
     - User registration with duplicate email rejection.
     - Password hashing in database (verifying BCrypt pattern, never plaintext).
     - Login, session regeneration, and logout.
     - Account isolation: User A entries cannot be read, updated, or deleted by User B.
     - Unauthenticated requests to `/api/meals` and `/api/targets` rejected with 401.
     - CSRF enforcement on state-changing requests.
6. **Step 6: MySQL & Browser Verification & Final Report**:
   - Execute Maven tests (`mvn test`) against H2.
   - Verify migrations and live server on local MySQL 8.0.
   - Conduct browser subagent testing of registration, login, meal logging, and logout.
   - Compile `docs/phase-5-implementation-report.md`.
