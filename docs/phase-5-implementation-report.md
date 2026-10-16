# FitKit Phase 5 — Authentication, Authorization & Security Hardening Implementation Report

## 1. Selected Authentication Approach & Architectural Rationale

### Choice: Server-Managed Cookie Session Authentication with Spring Security 6

FitKit is deployed as a unified single-origin Spring Boot application where frontend assets (HTML, CSS, JavaScript) and REST endpoints are served from the same host and port (`http://localhost:8080/`).

For this architecture, **server-managed session authentication using HTTP cookies** was selected over stateless JWT for the following reasons:
1. **Security Posture**: JWT tokens stored in browser `localStorage` or `sessionStorage` are vulnerable to Cross-Site Scripting (XSS) exfiltration. Session cookies marked `HttpOnly` cannot be read by JavaScript.
2. **Immediate Session Invalidation**: Server-side sessions can be instantly invalidated upon logout, user deactivation, or password change. Stateless JWTs cannot be revoked prior to expiration without maintaining a server-side blacklist, which reintroduces statefulness.
3. **Session Fixation Prevention**: Spring Security's `changeSessionId()` generates a new session identifier upon successful authentication, preventing session hijacking attacks.
4. **CSRF Protection**: State-changing requests (`POST`, `PUT`, `DELETE`) are guarded against Cross-Site Request Forgery via `CookieCsrfTokenRepository` (`SameSite=Lax`, `X-XSRF-TOKEN` header pattern), paired with Spring Security 6's `CsrfTokenRequestAttributeHandler` and `CsrfCookieFilter`.
5. **Architectural Simplicity**: Avoids redundant cryptographic signature verification libraries and key management overhead, perfectly aligning with Spring Boot 3.3.4 standards.

---

## 2. Files Created and Modified

### Created Files
- `src/main/resources/db/migration/V5__create_users_table_and_auth_schema.sql`: Database migration creating the `users` table, unique index on `email`, seeding `demo@fitkit.local`, and migrating legacy `default-user` meal/target records.
- `src/main/java/com/fitkit/domain/enums/UserRole.java`: Role enumeration (`ROLE_USER`, `ROLE_ADMIN`).
- `src/main/java/com/fitkit/domain/entity/User.java`: JPA user entity with normalized lowercase email, BCrypt password hash, and audit timestamps.
- `src/main/java/com/fitkit/repository/UserRepository.java`: Spring Data repository for user lookups (`findByEmail`, `existsByEmail`).
- `src/main/java/com/fitkit/dto/RegisterRequestDto.java`: Registration request payload with Jakarta validation (`@Email`, `@Size(min=8)`).
- `src/main/java/com/fitkit/dto/LoginRequestDto.java`: Login request payload with email and password fields.
- `src/main/java/com/fitkit/dto/UserProfileDto.java`: Safe user profile DTO containing user ID, email, full name, and role (never exposing password hash).
- `src/main/java/com/fitkit/service/CustomUserDetailsService.java`: Bridges FitKit `User` entity to Spring Security's `UserDetails`.
- `src/main/java/com/fitkit/service/AuthService.java`: Core authentication service handling registration, password hashing, session fixation mitigation, logout, and `@PostConstruct` demo user seeding.
- `src/main/java/com/fitkit/controller/AuthController.java`: REST controller exposing `/api/auth/register`, `/api/auth/login`, `/api/auth/logout`, `/api/auth/me`, and `/api/auth/csrf`.
- `src/main/java/com/fitkit/config/SecurityConfig.java`: Central Spring Security 6 configuration defining security filter chain, authentication provider, public/protected route rules, CSRF token handling, and custom JSON 401/403 handlers.
- `src/test/java/com/fitkit/SecurityAndAccountIsolationTest.java`: JUnit 5 tests asserting registration, duplicate rejection, BCrypt hashing, session fixation, and complete User A vs User B account isolation.
- `src/test/java/com/fitkit/WebSecurityEndpointProtectionTest.java`: MockMvc tests verifying 401 Unauthorized for unauthenticated requests and 200 OK for authenticated sessions.
- `docs/phase-5-security-audit.md`: Formal read-only audit deliverable.
- `docs/phase-5-implementation-report.md`: This comprehensive implementation report.

### Modified Files
- `pom.xml`: Added `spring-boot-starter-security` and `spring-security-test` dependencies.
- `src/main/java/com/fitkit/controller/MealLogController.java`: Refactored all endpoints (`/entries`, `/history`, `/migrate`) to derive identity strictly from `Principal principal.getName()`. Completely eliminated client-provided `userId` parameters.
- `src/main/java/com/fitkit/controller/UserTargetController.java`: Refactored target retrieval and mutation to use `Principal principal.getName()`. Removed client-provided `userId` parameter.
- `src/main/java/com/fitkit/service/MealLoggingService.java`: Hardened ownership validation in `updateMealEntry` and `deleteMealEntry` using case-insensitive email comparisons (`equalsIgnoreCase`).
- `src/main/resources/static/index.html`: Integrated `#view-auth` container with Sign In and Register tabs, demo credential filler, user profile avatar badge in sidebar/topbar, and Sign Out buttons.
- `src/main/resources/static/css/styles.css`: Added responsive styles for `.auth-container`, `.auth-card`, `.auth-tabs`, `.auth-btn-demo`, and user profile elements, strictly preserving the warm ivory, charcoal, and terracotta design system.
- `src/main/resources/static/js/app.js`: Replaced hardcoded `default-user` with dynamic authenticated session state, added CSRF-aware `apiFetch`, implemented login/register/logout workflows, and handled 401 redirects.
- `src/test/java/com/fitkit/MealLoggingIntegrationTest.java`: Updated integration tests to provide explicit user IDs matching the hardened service signatures.

---

## 3. Database Migrations Applied

### Migration `V5__create_users_table_and_auth_schema.sql`

```sql
-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(191) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(120),
    role VARCHAR(32) NOT NULL DEFAULT 'ROLE_USER',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_users_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_users_email ON users(email);
```

#### Migration Execution Log against MySQL 8.0:
```text
2026-10-15T20:58:34.005+05:30 INFO org.flywaydb.core.FlywayExecutor : Database: jdbc:mysql://localhost:3306/fitkit_db (MySQL 8.0)
2026-10-15T20:58:34.055+05:30 INFO o.f.core.internal.command.DbValidate : Successfully validated 5 migrations
2026-10-15T20:58:34.069+05:30 INFO o.f.core.internal.command.DbMigrate : Current version of schema `fitkit_db`: 4
2026-10-15T20:58:34.083+05:30 INFO o.f.core.internal.command.DbMigrate : Migrating schema `fitkit_db` to version "5 - create users table and auth schema"
2026-10-15T20:58:34.168+05:30 INFO o.f.core.internal.command.DbMigrate : Successfully applied 1 migration to schema `fitkit_db`, now at version v5
```

---

## 4. Public Versus Protected Endpoints

| Endpoint | HTTP Method | Access Level | Description |
|---|---|---|---|
| `/` | `GET` | **Public** | Main HTML entry point |
| `/index.html` | `GET` | **Public** | Static application dashboard & auth view |
| `/css/**`, `/js/**`, `/favicon.ico` | `GET` | **Public** | Static stylesheet, client scripts, icons |
| `/api/auth/register` | `POST` | **Public** | User registration with input validation |
| `/api/auth/login` | `POST` | **Public** | Session login and authentication |
| `/api/auth/logout` | `POST` | **Public** | Session invalidation and cookie clearing |
| `/api/auth/csrf` | `GET` | **Public** | CSRF token initialization |
| `/api/foods/**` | `GET` | **Public** | Canonical food catalog, nutrition info, categories |
| `/api/recipes/**` | `GET`, `POST` | **Public** | Reference recipes and deterministic yield engine |
| `/api/stats/**` | `GET` | **Public** | System catalog and recipe counts |
| `/api/auth/me` | `GET` | **Protected** | Authenticated user profile (401 if unauthenticated) |
| `/api/targets` | `GET` | **Protected** | Get authenticated user's calorie and macro targets |
| `/api/targets` | `PUT` | **Protected** | Update authenticated user's calorie and macro targets |
| `/api/meals` | `GET` | **Protected** | Get authenticated user's daily meal log |
| `/api/meals/entries` | `POST` | **Protected** | Add food/recipe to authenticated user's meal log |
| `/api/meals/entries/{id}` | `PUT` | **Protected** | Edit portion/multiplier for owned entry |
| `/api/meals/entries/{id}` | `DELETE` | **Protected** | Delete owned meal entry |
| `/api/meals/history` | `GET` | **Protected** | 7-day progress history for authenticated user |
| `/api/meals/migrate` | `POST` | **Protected** | Migrate localStorage entries into authenticated account |

---

## 5. How Identity and Ownership Are Enforced

### 1. Trusted Principal Identity
No client-supplied `userId` is ever accepted as proof of identity.
In `MealLogController` and `UserTargetController`:
```java
@GetMapping
public ResponseEntity<DailyMealLogResponseDto> getDailyMealLog(
        @RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        Principal principal) {
    if (principal == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    String authenticatedUserId = principal.getName();
    DailyMealLogResponseDto response = mealLoggingService.getOrCreateDailyMealLog(authenticatedUserId, targetDate);
    return ResponseEntity.ok(response);
}
```
Attempting to send `?userId=victim@example.com` has zero effect, as controllers do not inspect query parameters or request bodies for identity.

### 2. Strict Entity Ownership Checks
When an entry is modified or deleted via `/api/meals/entries/{id}`:
```java
MealEntry entry = mealEntryRepository.findById(entryId)
        .orElseThrow(() -> new EntityNotFoundException("Meal entry not found with id: " + entryId));

if (!entry.getMealLog().getUserId().equalsIgnoreCase(authenticatedUserId)) {
    throw new SecurityException("Access denied: You do not own this meal entry.");
}
```
If User B attempts to delete or update an entry belonging to User A, a `SecurityException` (`403 Forbidden`) is triggered, preventing Insecure Direct Object References (IDOR).

### 3. Isolated Repository Scoping
Database queries retrieving daily logs, target profiles, and history explicitly scope by `userId = :authenticatedUserId`:
```java
mealLogRepository.findByUserIdAndLogDate(userId, date);
userTargetRepository.findByUserId(userId);
mealLogRepository.findByUserIdAndLogDateBetweenOrderByLogDateAsc(userId, startDate, endDate);
```
Data across distinct users is strictly partitioned in the database.

---

## 6. How Legacy Development Records Were Migrated

### 1. Dedicated Seed User
To prevent orphaned records or accidental exposure, a verified demo user was established:
- **Email**: `demo@fitkit.local`
- **Password**: `FitKit2026!` (BCrypt hashed)
- **Role**: `ROLE_USER`

### 2. Backward-Compatible Database Assignment
Migration `V5` reassigned all historical records previously associated with `default-user`:
```sql
UPDATE user_targets SET user_id = 'demo@fitkit.local' WHERE user_id = 'default-user';
UPDATE meal_logs SET user_id = 'demo@fitkit.local' WHERE user_id = 'default-user';
```
No historical meal entries or nutrition data were deleted. When the demo user logs in, their complete 7-day progress history and targets remain intact.

### 3. Safe Frontend Migration (`/api/meals/migrate`)
Legacy `localStorage` entries are imported only into the account of the currently authenticated principal:
```java
@PostMapping("/migrate")
public ResponseEntity<Map<String, Object>> migrateLocalStorageDiary(
        @Valid @RequestBody DiaryMigrationRequestDto request,
        Principal principal) {
    String authenticatedUserId = principal.getName();
    int imported = mealLoggingService.migrateLegacyEntries(authenticatedUserId, request);
    return ResponseEntity.ok(Map.of("success", true, "importedCount", imported));
}
```
Users cannot upload data into another user's journal.

---

## 7. Tests Executed and Actual Results

### Automated Test Suite: 40 Tests Executed, 40 Passed, 0 Failures

```text
[INFO] Results:
[INFO] 
[INFO] Tests run: 40, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] Total time:  32.523 s
[INFO] ------------------------------------------------------------------------
```

### Security & Account Isolation Tests (`SecurityAndAccountIsolationTest.java` - 7/7 Passed)
- `testSuccessfulRegistrationAndPasswordHashing`: Verifies user registration, ensures passwords are stored as BCrypt hashes starting with `$2a$`, and confirms raw passwords are never stored.
- `testDuplicateEmailRejection`: Asserts `IllegalArgumentException` when attempting to register an existing email.
- `testSuccessfulAuthentication`: Validates login using valid credentials.
- `testInvalidLoginRejection`: Asserts `BadCredentialsException` on invalid passwords.
- `testUserAccountIsolation_UserBCannotModifyUserAEntry`: Verifies User B receives `SecurityException` when attempting to edit User A's meal entry.
- `testUserAccountIsolation_UserBCannotDeleteUserAEntry`: Verifies User B receives `SecurityException` when attempting to delete User A's meal entry.
- `testUserTargetIsolation_UserAandBHaveIndependentTargets`: Asserts that changing User A's calorie targets does not affect User B's targets.

### Web Security & Route Protection Tests (`WebSecurityEndpointProtectionTest.java` - 5/5 Passed)
- `unauthenticatedRequestToMeals_Returns401`: Verifies `GET /api/meals` without a session returns 401 Unauthorized JSON response.
- `unauthenticatedRequestToTargets_Returns401`: Verifies `GET /api/targets` returns 401 Unauthorized.
- `publicEndpoints_PermitAll`: Verifies `GET /api/foods`, `GET /api/recipes`, and `GET /api/stats` respond with 200 OK without authentication.
- `authenticatedUser_CanAccessProtectedEndpoints`: Verifies `@WithMockUser` session grants access to `/api/meals` and `/api/targets`.
- `csrfProtection_StateChangingEndpointRequiresCsrfOrSession`: Confirms CSRF enforcement on state-changing user endpoints.

### Nutrition Core Regression Tests (28/28 Passed)
- `MealLoggingIntegrationTest`: 7 tests passed (meal logging, quantity updates, nutrient snapshots, 7-day progress, deletion).
- `DatabaseConstraintsTest`: 5 tests passed (foreign keys, uniqueness constraints, positive weights).
- `DataImportPipelineTest`: 4 tests passed (catalog ingestion, recipe yield initialization).
- `NutritionNormalizationTest`: 8 tests passed (Atwater energy validation, macro calculations).
- `RecipeCalculationTest`: 4 tests passed (raw vs cooked yields, moisture retention).

---

## 8. Live MySQL 8.0 Verification & Browser Subagent Results

### Live Database Verification
- Connected to MySQL 8.0 on `localhost:3306/fitkit_db`.
- Flyway schema table verified at version 5.
- Users table created with unique email index.
- Demo user seeded and verified.

### Browser End-to-End Test Execution

| Step | Action | Expected Result | Actual Result | Status |
|---|---|---|---|---|
| 1 | Visit `http://localhost:8080/` | Unauthenticated visitor sees warm ivory Auth View | Sign In / Create Account forms displayed | **PASS** |
| 2 | Click "Fill Demo Credentials" | Form populated with `demo@fitkit.local` | Email and password filled | **PASS** |
| 3 | Click "Sign In" | Session authenticated, dashboard loaded | Dashboard loaded with demo user avatar 'D' | **PASS** |
| 4 | Add Basmati Rice to Lunch | Entry added via CSRF-authenticated request | Calorie card and lunch section updated | **PASS** |
| 5 | Click "Sign Out" | Session invalidated on server | Redirected to clean Auth screen | **PASS** |
| 6 | Create Account (`alice@fitkit.local`) | New user registered and logged in | Isolated empty dashboard loaded (0 kcal) | **PASS** |

### Screenshots Captured
1. `fitkit_auth_screen_1792078787686.png`: Unauthenticated auth card with Sign In & Register tabs.
2. `fitkit_authenticated_dashboard_1792078806652.png`: Demo user dashboard with nutrition targets and history.
3. `fitkit_food_logged_success_1792078857828.png`: Basmati rice successfully logged to Lunch.
4. `fitkit_logged_out_1792078874590.png`: Signed-out state with invalidated session.
5. `fitkit_new_account_dashboard_1792078934761.png`: Alice Runner's private, completely isolated dashboard.

---

## 9. Remaining Security Limitations & Recommendations

1. **Email Verification**: Currently, accounts are activated immediately upon registration. In a production internet deployment, an email verification token should be sent to confirm mailbox ownership.
2. **Password Reset Workflow**: A secure token-based forgot password mechanism should be implemented.
3. **Rate Limiting / Brute-Force Protection**: While BCrypt introduces adaptive hashing latency, adding an IP-based rate limiter (e.g. Bucket4j or Redis-backed rate limiting) on `/api/auth/login` is recommended before public internet deployment.
4. **HTTPS / Secure Cookies**: Session cookies are configured with `SameSite=Lax`. In production under TLS, the `Secure` flag should be enabled via `server.servlet.session.cookie.secure=true`.

---

## 10. Steps for Running the Application Locally

### Prerequisites
- Java 17+ installed and on `PATH`.
- Maven 3.8+ installed.
- MySQL 8.0 running on `localhost:3306` with database `fitkit_db` (or configure via environment variables).

### 1. Database Configuration
Ensure `src/main/resources/application.properties` or environment variables match your local MySQL credentials:
```properties
spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/fitkit_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC}
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:<your_db_password>}
```

### 2. Run Database Migrations and Application
```bash
mvn clean spring-boot:run
```
Flyway will automatically apply migrations `V1` through `V5`, creating tables, indexes, and seed accounts.

### 3. Run Automated Tests
```bash
mvn test
```
Runs all 40 unit and integration tests using an isolated H2 in-memory database.

### 4. Access the Application
Open a modern web browser and navigate to:
```
http://localhost:8080/
```
- **Demo Account**: `demo@fitkit.local` / `FitKit2026!`
- Or click the **Create Account** tab to register a new isolated account.
