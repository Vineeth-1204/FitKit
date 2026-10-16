# FitKit Phase 6.1 — Nutrition Data Reconciliation & Security Configuration Remediation Report

**Date:** October 15, 2026  
**System Version:** FitKit 1.0.0 (Phase 6.1 Remediation)  
**Database:** MySQL 8.0 (Primary) / H2 In-Memory (Test Isolation)  
**Flyway Migration Status:** Schema `fitkit_db` migrated to Version `7`  
**Test Suite Status:** 49 tests passed, 0 failures, 0 skipped  

---

## 1. Executive Summary & Scope

Phase 6.1 is a targeted remediation phase initiated to audit, reconcile, and resolve discrepancies identified during the review of Phase 6 deliverables. In strict alignment with instructions, this phase introduced no unnecessary UI redesigns, no recipe engine rewrites, and preserved all user data and existing catalog provenance.

### Primary Objectives Delivered:
1. **Nutrition Data Reconciliation:** Reconciled discrepancies across the initial `V6` migration, active database records, `food_nutrients` entries, and Phase 6 documentation for all 7 newly introduced staple ingredients (Semolina, Onion, Tomato, Green Chili, Cumin, Turmeric, and Mustard Oil). Authoritative **Indian Food Composition Tables (IFCT 2017)** records from the National Institute of Nutrition (ICMR-NIN) were established with exact macronutrient baselines and a clear distinction between source-reported energy and calculated Atwater energy.
2. **Forward-Only Flyway Migration:** In compliance with Flyway best practices, the previously applied `V6` migration was left untouched. A new forward-only migration [`V7__reconcile_staple_nutrition_and_codes.sql`](file:///d:/Projects/WebProgramming/src/main/resources/db/migration/V7__reconcile_staple_nutrition_and_codes.sql) was created, tested, and applied cleanly to the live MySQL 8.0 `fitkit_db` database.
3. **Hardcoded Database Credential Removal & Fail-Fast Enforcement:** Removed plaintext password fallbacks from configuration defaults (`application.properties`) and documentation. Configured Spring datasource password injection via environment variables (`${DB_PASSWORD:${SPRING_DATASOURCE_PASSWORD}}`), with verified fail-fast termination when credentials are missing.
4. **Demo Account Seeding Restriction:** Changed default seeding configuration from `true` to `false` in both code (`AuthService.java`) and configuration (`application.properties`). Demo account seeding is explicitly isolated to the `dev` profile (`application-dev.properties`). Verified that disabling demo seeding preserves existing accounts and that user registration/authentication operate seamlessly without demo seeding.
5. **Full Regression & Verification:** All 49 automated tests in Maven passed with 0 failures on H2. Browser subagent testing confirmed the food library, custom recipe lab, and UI styling remain fully operational.

---

## 2. Nutrition Data Audit & Discrepancy Analysis

### 2.1 Root Cause of Discrepancies
During Phase 6, two conflicting data representations were introduced:
- **`V6` Migration & `phase-6-nutrition-data-quality.md`:** Seeded foods using legacy NVIF (1989) approximate numbers and total carbohydrate by difference (e.g., Cumin at 375 kcal, Turmeric at 349 kcal, Semolina as `IFCT_A018` at 340 kcal). Furthermore, several food codes from older numbering systems were used (`C012` for onion, which in IFCT 2017 is Brussels sprouts; `C035` for tomato, which in IFCT 2017 is Dill leaves).
- **`phase-6-implementation-report.md`:** Summarized IFCT 2017 specific energy values (e.g., Semolina at 333.7 kcal, Onion at 48.5 kcal, Tomato at 20.8 kcal, Mustard Oil at 884 kcal), creating an inconsistency between what the implementation report claimed and what `V6` had inserted into the MySQL database.

### 2.2 Reconciled Authoritative Source Records (IFCT 2017, ICMR-NIN)
Each food was audited against the peer-reviewed **Indian Food Composition Tables (IFCT 2017)** published by the National Institute of Nutrition (ICMR):

| ID | Food Name | Old Code (`V6`) | Authoritative Code (`V7`) | Scientific Name | Source Energy (kJ / kcal) | Atwater Estimate (kcal) | P (g) | Available C (g) | F (g) | Fiber (g) | Moisture (g) | Ash (g) | Quality Status |
|:---|:---|:---|:---|:---|:---|:---|:---|:---|:---|:---|:---|:---|:---|
| **27** | Semolina / Suji / Rava | `IFCT_A018` | **`IFCT_A022`** | *Triticum aestivum* | 1396 kJ (333.7 kcal) | 326.4 kcal | 11.38 | 68.43 | 0.80 | 9.72 | 8.94 | 0.73 | `SOURCE_VERIFIED` |
| **28** | Onion, fresh raw | `IFCT_C012` | **`IFCT_G017`** | *Allium cepa* | 203 kJ (48.5 kcal) | 45.6 kcal | 1.20 | 9.30 | 0.40 | 2.50 | 84.30 | 0.80 | `SOURCE_VERIFIED` |
| **29** | Tomato, ripe hybrid raw | `IFCT_C035` | **`IFCT_D075`** | *Solanum lycopersicum* | 87 kJ (20.8 kcal) | 19.8 kcal | 0.90 | 3.60 | 0.20 | 1.70 | 93.10 | 0.50 | `SOURCE_VERIFIED` |
| **30** | Green Chili, fresh raw | `IFCT_D005` | **`IFCT_G011`** | *Capsicum annuum* | 191 kJ (45.7 kcal) | 43.0 kcal | 2.60 | 6.80 | 0.60 | 6.80 | 81.30 | 0.90 | `SOURCE_VERIFIED` |
| **31** | Cumin Seeds (Jeera) | `IFCT_D004` | **`IFCT_G025`** | *Cuminum cyminum* | 1274 kJ (304.5 kcal) | 356.2 kcal | 18.70 | 36.60 | 15.00 | 22.62 | 8.10 | 7.70 | `SOURCE_VERIFIED` |
| **32** | Turmeric Powder (Haldi) | `IFCT_D028` | **`IFCT_G026`** | *Curcuma longa* | 1174 kJ (280.6 kcal) | 313.6 kcal | 6.30 | 64.90 | 3.20 | 21.10 | 11.40 | 6.80 | `SOURCE_VERIFIED` |
| **33** | Mustard Oil, cold-pressed | `IFCT_T017` | **`IFCT_T017`** | *Brassica juncea* | 0 kJ (Source Anomaly) | 884.0 / 900.0 kcal | 0.00 | 0.00 | 100.00 | 0.00 | 0.00 | 0.00 | `CORRECTED_ANOMALY` |

### 2.3 Source Energy vs. Atwater Macronutrient Estimation
FitKit records both the authoritative source energy and the proximate breakdown to ensure mathematical transparency:
- **Semolina (`IFCT_A022`):** IFCT 2017 reports $1396\text{ kJ}$ ($333.65\text{ kcal}$, rounded to $333.7\text{ kcal}$). The standard 4-4-9 Atwater general factor estimate is $11.38 \times 4 + 68.43 \times 4 + 0.80 \times 9 = 326.44\text{ kcal}$.
- **Onion (`IFCT_G017`):** IFCT 2017 reports $203\text{ kJ}$ ($48.5\text{ kcal}$). Available carbs are $9.30\text{ g}$ ($2.50\text{ g}$ dietary fiber is reported separately). Atwater estimate is $1.2 \times 4 + 9.3 \times 4 + 0.4 \times 9 = 45.6\text{ kcal}$.
- **Tomato (`IFCT_D075`):** IFCT 2017 reports $87\text{ kJ}$ ($20.8\text{ kcal}$). Available carbs are $3.60\text{ g}$. Atwater estimate is $0.9 \times 4 + 3.6 \times 4 + 0.2 \times 9 = 19.8\text{ kcal}$.
- **Green Chili (`IFCT_G011`):** IFCT 2017 reports $191\text{ kJ}$ ($45.7\text{ kcal}$). Available carbs are $6.80\text{ g}$ with $6.80\text{ g}$ dietary fiber. Atwater estimate is $43.0\text{ kcal}$.
- **Cumin Seeds (`IFCT_G025`):** IFCT 2017 reports $1274\text{ kJ}$ ($304.5\text{ kcal}$). Legacy V6 had 375 kcal because older NVIF tables counted carbohydrates by difference including fiber ($44.2\text{ g}$). In IFCT 2017, available carbohydrates is $36.60\text{ g}$ and fiber is $22.62\text{ g}$.
- **Turmeric Powder (`IFCT_G026`):** IFCT 2017 reports $1174\text{ kJ}$ ($280.6\text{ kcal}$). Legacy V6 had 349 kcal from older NVIF total carbohydrate. Available carbs is $64.90\text{ g}$ and dietary fiber is $21.10\text{ g}$.
- **Mustard Oil (`IFCT_T017`):** In the raw IFCT 2017 publication, energy for T017 was printed as 0 kJ. Because 100g cold-pressed oil consists entirely of lipids, energy was reconciled using the FAO/INFOODS specific vegetable oil energy factor ($8.84\text{ kcal/g} \times 100 = 884.0\text{ kcal} / 3700.0\text{ kJ}$); standard Atwater ($9.0\text{ kcal/g}$) yields $900.0\text{ kcal}$. Both figures are documented in `correction_notes`, and quality status is explicitly marked `CORRECTED_ANOMALY`.

---

## 3. Database Migration Implementation (`V7`)

In accordance with database migration safety standards, existing migration `V6` was not edited. A forward-only migration [`V7__reconcile_staple_nutrition_and_codes.sql`](file:///d:/Projects/WebProgramming/src/main/resources/db/migration/V7__reconcile_staple_nutrition_and_codes.sql) was created:

```sql
-- Reconcile Semolina to IFCT 2017 A022
UPDATE foods 
SET food_code = 'IFCT_A022', source_record_id = 'A022', scientific_name = 'Triticum aestivum', updated_at = CURRENT_TIMESTAMP
WHERE id = 27;

UPDATE food_nutrients 
SET energy_kcal = 333.70, energy_kj = 1396.00, protein_g = 11.38, available_carbohydrates_g = 68.43, total_fat_g = 0.80, 
    dietary_fiber_g = 9.72, moisture_water_g = 8.94, ash_g = 0.73, raw_source_energy = '1396 kJ', 
    quality_status = 'SOURCE_VERIFIED', 
    correction_notes = 'Reconciled to IFCT 2017 entry A022 (Triticum aestivum, semolina/suji). Source energy 1396 kJ (333.7 kcal). Atwater estimate: 326.4 kcal.'
WHERE food_id = 27;

-- Reconcile Onion to IFCT 2017 G017
UPDATE foods 
SET food_code = 'IFCT_G017', source_record_id = 'G017', scientific_name = 'Allium cepa', updated_at = CURRENT_TIMESTAMP
WHERE id = 28;

UPDATE food_nutrients 
SET energy_kcal = 48.50, energy_kj = 203.00, protein_g = 1.20, available_carbohydrates_g = 9.30, total_fat_g = 0.40, 
    dietary_fiber_g = 2.50, moisture_water_g = 84.30, ash_g = 0.80, raw_source_energy = '203 kJ', 
    quality_status = 'SOURCE_VERIFIED', 
    correction_notes = 'Reconciled to IFCT 2017 entry G017 (Allium cepa, big onion). Source energy 203 kJ (48.5 kcal). Atwater estimate: 45.6 kcal.'
WHERE food_id = 28;

-- Reconcile Tomato to IFCT 2017 D075
UPDATE foods 
SET food_code = 'IFCT_D075', source_record_id = 'D075', scientific_name = 'Solanum lycopersicum', updated_at = CURRENT_TIMESTAMP
WHERE id = 29;

UPDATE food_nutrients 
SET energy_kcal = 20.80, energy_kj = 87.00, protein_g = 0.90, available_carbohydrates_g = 3.60, total_fat_g = 0.20, 
    dietary_fiber_g = 1.70, moisture_water_g = 93.10, ash_g = 0.50, raw_source_energy = '87 kJ', 
    quality_status = 'SOURCE_VERIFIED', 
    correction_notes = 'Reconciled to IFCT 2017 entry D075 (Solanum lycopersicum, ripe hybrid tomato). Source energy 87 kJ (20.8 kcal). Atwater estimate: 19.8 kcal.'
WHERE food_id = 29;

-- Reconcile Green Chili to IFCT 2017 G011
UPDATE foods 
SET food_code = 'IFCT_G011', source_record_id = 'G011', scientific_name = 'Capsicum annuum', updated_at = CURRENT_TIMESTAMP
WHERE id = 30;

UPDATE food_nutrients 
SET energy_kcal = 45.70, energy_kj = 191.00, protein_g = 2.60, available_carbohydrates_g = 6.80, total_fat_g = 0.60, 
    dietary_fiber_g = 6.80, moisture_water_g = 81.30, ash_g = 0.90, raw_source_energy = '191 kJ', 
    quality_status = 'SOURCE_VERIFIED', 
    correction_notes = 'Reconciled to IFCT 2017 entry G011 (Capsicum annuum, green chili). Source energy 191 kJ (45.7 kcal). Atwater estimate: 43.0 kcal.'
WHERE food_id = 30;

-- Reconcile Cumin Seeds to IFCT 2017 G025
UPDATE foods 
SET food_code = 'IFCT_G025', source_record_id = 'G025', scientific_name = 'Cuminum cyminum', updated_at = CURRENT_TIMESTAMP
WHERE id = 31;

UPDATE food_nutrients 
SET energy_kcal = 304.50, energy_kj = 1274.00, protein_g = 18.70, available_carbohydrates_g = 36.60, total_fat_g = 15.00, 
    dietary_fiber_g = 22.62, moisture_water_g = 8.10, ash_g = 7.70, raw_source_energy = '1274 kJ', 
    quality_status = 'SOURCE_VERIFIED', 
    correction_notes = 'Reconciled to IFCT 2017 entry G025 (Cuminum cyminum, cumin seeds). Source energy 1274 kJ (304.5 kcal). Legacy V6 used old NVIF total carbohydrate (375 kcal). Atwater estimate: 356.2 kcal.'
WHERE food_id = 31;

-- Reconcile Turmeric Powder to IFCT 2017 G026
UPDATE foods 
SET food_code = 'IFCT_G026', source_record_id = 'G026', scientific_name = 'Curcuma longa', updated_at = CURRENT_TIMESTAMP
WHERE id = 32;

UPDATE food_nutrients 
SET energy_kcal = 280.60, energy_kj = 1174.00, protein_g = 6.30, available_carbohydrates_g = 64.90, total_fat_g = 3.20, 
    dietary_fiber_g = 21.10, moisture_water_g = 11.40, ash_g = 6.80, raw_source_energy = '1174 kJ', 
    quality_status = 'SOURCE_VERIFIED', 
    correction_notes = 'Reconciled to IFCT 2017 entry G026 (Curcuma longa, turmeric powder). Source energy 1174 kJ (280.6 kcal). Legacy V6 used old NVIF total carbohydrate (349 kcal). Atwater estimate: 313.6 kcal.'
WHERE food_id = 32;

-- Reconcile Mustard Oil (IFCT T017)
UPDATE foods 
SET food_code = 'IFCT_T017', source_record_id = 'T017', scientific_name = 'Brassica juncea', updated_at = CURRENT_TIMESTAMP
WHERE id = 33;

UPDATE food_nutrients 
SET energy_kcal = 884.00, energy_kj = 3700.00, protein_g = 0.00, available_carbohydrates_g = 0.00, total_fat_g = 100.00, 
    dietary_fiber_g = 0.00, moisture_water_g = 0.00, ash_g = 0.00, raw_source_energy = '0 kJ (anomaly)', 
    quality_status = 'CORRECTED_ANOMALY', 
    correction_notes = 'CORRECTION AUDIT: Source recorded 0 kJ despite 100g lipids. Estimated using FAO/INFOODS specific vegetable oil factor 8.84 kcal/g (884.0 kcal / 3700 kJ); standard Atwater 9.0 kcal/g yields 900.0 kcal (3765.6 kJ).'
WHERE food_id = 33;
```

### 3.1 Migration Verification on Live MySQL 8.0
Upon launching Spring Boot against MySQL 8.0 `fitkit_db`, Flyway automatically executed:
```text
2026-10-15T22:59:15.257+05:30  INFO 6796 --- [fitkit-nutrition-core] [           main] org.flywaydb.core.FlywayExecutor         : Database: jdbc:mysql://localhost:3306/fitkit_db (MySQL 8.0)
2026-10-15T22:59:15.307+05:30  INFO 6796 --- [fitkit-nutrition-core] [           main] o.f.core.internal.command.DbValidate     : Successfully validated 7 migrations (execution time 00:00.028s)
2026-10-15T22:59:15.326+05:30  INFO 6796 --- [fitkit-nutrition-core] [           main] o.f.core.internal.command.DbMigrate      : Current version of schema `fitkit_db`: 6
2026-10-15T22:59:15.347+05:30  INFO 6796 --- [fitkit-nutrition-core] [           main] o.f.core.internal.command.DbMigrate      : Migrating schema `fitkit_db` to version "7 - reconcile staple nutrition and codes"
2026-10-15T22:59:15.398+05:30  INFO 6796 --- [fitkit-nutrition-core] [           main] o.f.core.internal.command.DbMigrate      : Successfully applied 1 migration to schema `fitkit_db`, now at version v7 (execution time 00:00.024s)
```

---

## 4. Security Configuration Remediation

### 4.1 Plaintext Database Credential Removal & Fail-Fast Verification
1. **Sanitization:** Audited and eliminated all hardcoded password values from:
   - [`src/main/resources/application.properties`](file:///d:/Projects/WebProgramming/src/main/resources/application.properties)
   - [`docs/phase-6-implementation-report.md`](file:///d:/Projects/WebProgramming/docs/phase-6-implementation-report.md)
   - Verified that no repository configuration, script, or documentation leaks credentials.
2. **Environment Variable Configuration:**
   `spring.datasource.password=${DB_PASSWORD:${SPRING_DATASOURCE_PASSWORD}}`
   Requires the credential via `DB_PASSWORD` or `SPRING_DATASOURCE_PASSWORD`.
3. **Fail-Fast Testing:**
   Executed startup test with both variables unset. Verified that Spring Boot failed immediately with:
   `Unable to obtain connection from database: Access denied for user 'root'@'localhost'`
   `BUILD FAILURE`
   No secret values were logged during startup failure.
4. **Credential Rotation Guidance:**
   In line with security standards and authorization boundaries, local database accounts were not modified without authorization. For production deployment, the database administrator must rotate the database user password in accordance with enterprise credential rotation policies.

### 4.2 Restricting Demo-Account Seeding
1. **Disabled by Default:**
   - In [`AuthService.java`](file:///d:/Projects/WebProgramming/src/main/java/com/fitkit/service/AuthService.java):
     `@Value("${fitkit.auth.seed-demo:false}") private boolean seedDemoEnabled;`
   - In [`application.properties`](file:///d:/Projects/WebProgramming/src/main/resources/application.properties):
     `fitkit.auth.seed-demo=${SEED_DEMO_ACCOUNT:false}`
2. **Explicit Development Profile Isolation:**
   Created [`application-dev.properties`](file:///d:/Projects/WebProgramming/src/main/resources/application-dev.properties) with `fitkit.auth.seed-demo=true` so demo seeding is only active when the `dev` profile is explicitly enabled or `SEED_DEMO_ACCOUNT=true` is set.
3. **Data Preservation Verified:**
   Disabling demo seeding does not purge, alter, or drop existing demo accounts or their associated meal entries and recipes in MySQL.
4. **Authentication & Registration Independence:**
   Verified via automated test `testAuthAndRegistrationWithoutDemoSeeding()` that new user registration, BCrypt credential hashing, user target auto-provisioning, and session login function without dependency on demo seeding.

---

## 5. Automated Regression & Test Suite Verification

The full Maven test suite was executed:
```text
[INFO] Results:
[INFO] 
[INFO] Tests run: 49, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  30.044 s
```

### Key Automated Tests Added / Verified:
- [`CustomRecipeAndDataQualityTest.java`](file:///d:/Projects/WebProgramming/src/test/java/com/fitkit/CustomRecipeAndDataQualityTest.java):
  - `testPhase61ReconciledStapleIngredients`: Verifies all 7 reconciled food items against exact IFCT 2017 source records, energy values, macronutrients, and quality statuses. Asserts that retired food codes (`IFCT_A018`, `IFCT_C012`, `IFCT_C035`, `IFCT_D005`, `IFCT_D004`, `IFCT_D028`) no longer exist.
  - `testCustomRecipeCalculationWithReconciledStaples`: Formulates a multi-ingredient recipe ("Homemade Suji Upma") using Semolina (`IFCT_A022`), Onion (`IFCT_G017`), Mustard Oil (`IFCT_T017`), and Cumin (`IFCT_G025`), verifying batch calories ($408.2\text{ kcal}$), cooked density ($116.64\text{ kcal/100g}$), and per-serving nutrition ($204.12\text{ kcal}$).
  - `testHistoricalMealSnapshotPreservationOnRecipeEdit`: Confirms that updating or modifying a recipe formulation preserves the exact historical snapshot in previously logged `meal_entries`.
  - `testRecipeCalculationsAndYield`: Confirms yield handling and absence of double yield multiplication.
  - `testRecipeValidationConstraints`: Enforces validation constraints against empty formulations or negative ingredient weights.
- [`SecurityAndAccountIsolationTest.java`](file:///d:/Projects/WebProgramming/src/test/java/com/fitkit/SecurityAndAccountIsolationTest.java):
  - `testAuthAndRegistrationWithoutDemoSeeding`: Verifies that user registration, target creation, and authentication operate without demo seeding. Confirms demo account preservation.
  - `testUserOwnershipIsolation`: Confirms User B cannot view, modify, or delete User A's custom recipes.
  - `testUserTargetIsolation` & `testWeeklyHistoryIsolation`: Confirms strict multi-tenant data separation.
- [`WebSecurityEndpointProtectionTest.java`](file:///d:/Projects/WebProgramming/src/test/java/com/fitkit/WebSecurityEndpointProtectionTest.java): Confirms unauthenticated access to `/api/custom-recipes/**`, `/api/foods/**`, `/api/meals/**`, and `/api/targets/**` returns `401 Unauthorized`.

---

## 6. Browser & Live REST API Verification

1. **REST API Verification (Port 8080):**
   Queried the running Spring Boot instance with an authenticated session:
   - `[IFCT_A022] Semolina / Suji / Rava` -> `333.7 kcal`, `P: 11.38g`, `C: 68.43g`, `F: 0.8g`, `Fib: 9.72g` (`SOURCE_VERIFIED`)
   - `[IFCT_G017] Onion, fresh raw` -> `48.5 kcal`, `P: 1.2g`, `C: 9.3g`, `F: 0.4g`, `Fib: 2.5g` (`SOURCE_VERIFIED`)
   - `[IFCT_D075] Tomato, ripe raw` -> `20.8 kcal`, `P: 0.9g`, `C: 3.6g`, `F: 0.2g`, `Fib: 1.7g` (`SOURCE_VERIFIED`)
   - `[IFCT_G011] Green Chili, fresh raw` -> `45.7 kcal`, `P: 2.6g`, `C: 6.8g`, `F: 0.6g`, `Fib: 6.8g` (`SOURCE_VERIFIED`)
   - `[IFCT_G025] Cumin Seeds (Jeera)` -> `304.5 kcal`, `P: 18.7g`, `C: 36.6g`, `F: 15.0g`, `Fib: 22.62g` (`SOURCE_VERIFIED`)
   - `[IFCT_G026] Turmeric Powder (Haldi)` -> `280.6 kcal`, `P: 6.3g`, `C: 64.9g`, `F: 3.2g`, `Fib: 21.1g` (`SOURCE_VERIFIED`)
   - `[IFCT_T017] Mustard Oil, cold-pressed` -> `884 kcal`, `P: 0g`, `C: 0g`, `F: 100g`, `Fib: 0g` (`CORRECTED_ANOMALY`)
2. **Browser Subagent Verification:**
   - Navigated to `http://localhost:8080/`.
   - Verified Food Library search results and badge metadata.
   - Verified Recipe & Cooking Yield Lab navigation and custom recipe card listings.
   - Verified that the Create Custom Recipe modal opens cleanly, adheres to the warm ivory (`#F7F4ED`), soft white (`#FFFDF8`), charcoal (`#292722`), and muted terracotta (`#B85F45`) palette, and closes cleanly without console errors.

---

## 7. Files Changed and Created

| File | Change Type | Description |
|:---|:---|:---|
| [`src/main/resources/db/migration/V7__reconcile_staple_nutrition_and_codes.sql`](file:///d:/Projects/WebProgramming/src/main/resources/db/migration/V7__reconcile_staple_nutrition_and_codes.sql) | **Created** | Forward-only Flyway migration reconciling food codes, nutrients, portions, and provenance for foods 27–33. |
| [`src/main/resources/application.properties`](file:///d:/Projects/WebProgramming/src/main/resources/application.properties) | **Modified** | Removed plaintext password default; set demo seeding default to `false`. |
| [`src/main/resources/application-dev.properties`](file:///d:/Projects/WebProgramming/src/main/resources/application-dev.properties) | **Created** | Development profile configuration explicitly enabling `fitkit.auth.seed-demo=true`. |
| [`src/main/java/com/fitkit/service/AuthService.java`](file:///d:/Projects/WebProgramming/src/main/java/com/fitkit/service/AuthService.java) | **Modified** | Changed `@Value` default of `fitkit.auth.seed-demo` to `false`. |
| [`src/test/java/com/fitkit/CustomRecipeAndDataQualityTest.java`](file:///d:/Projects/WebProgramming/src/test/java/com/fitkit/CustomRecipeAndDataQualityTest.java) | **Modified** | Added unit and integration tests verifying reconciled staple ingredients and custom recipe formulation. |
| [`src/test/java/com/fitkit/SecurityAndAccountIsolationTest.java`](file:///d:/Projects/WebProgramming/src/test/java/com/fitkit/SecurityAndAccountIsolationTest.java) | **Modified** | Added test verifying registration and authentication work independently of demo seeding. |
| [`docs/phase-6-nutrition-data-quality.md`](file:///d:/Projects/WebProgramming/docs/phase-6-nutrition-data-quality.md) | **Modified** | Updated Section 6 with reconciled IFCT 2017 codes, proximate values, and Atwater validation notes. |
| [`docs/phase-6-implementation-report.md`](file:///d:/Projects/WebProgramming/docs/phase-6-implementation-report.md) | **Modified** | Replaced Section 3 catalog table and Section 10 configuration hygiene text with sanitized, reconciled data. |
| [`docs/phase-6.1-remediation-report.md`](file:///d:/Projects/WebProgramming/docs/phase-6.1-remediation-report.md) | **Created** | This comprehensive remediation report documenting audit findings, migrations, security fixes, and test results. |

---

## 8. Remaining Limitations & Recommendations

1. **Production Credential Secret Store:** While application configuration now requires external environment variables and fails fast without defaults, enterprise production environments should inject secrets via a dedicated secrets vault (e.g. AWS Secrets Manager, HashiCorp Vault, or Kubernetes Secrets).
2. **Database Password Rotation in Production:** The local development credential was sanitized from source control. In production instances, the database administrator should ensure database user credentials are regularly rotated.
3. **Expanded Staple Ingredients Catalog:** With the core 7 staple ingredients (Semolina, Onion, Tomato, Green Chili, Cumin, Turmeric, and Mustard Oil) reconciled and verified, future phases can expand to ginger-garlic paste, curry leaves, and coriander leaves using the same IFCT 2017 provenance methodology.
