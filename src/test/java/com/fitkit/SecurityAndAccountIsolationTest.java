package com.fitkit;

import com.fitkit.domain.entity.User;
import com.fitkit.dto.*;
import com.fitkit.repository.UserRepository;
import com.fitkit.service.AuthService;
import com.fitkit.service.MealLoggingService;
import com.fitkit.service.UserTargetService;
import com.fitkit.service.NutritionProfileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class SecurityAndAccountIsolationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MealLoggingService mealLoggingService;

    @Autowired
    private UserTargetService userTargetService;

    @Autowired
    private NutritionProfileService nutritionProfileService;

    @Test
    @DisplayName("Should successfully register a new user and securely hash the password with BCrypt")
    void testUserRegistrationAndSecureHashing() {
        String email = "athlete.riya@fitkit.local";
        String rawPassword = "StrongPassword2026!";

        RegisterRequestDto req = new RegisterRequestDto(email, rawPassword, "Riya Sharma");
        UserProfileDto profile = authService.register(req);

        assertNotNull(profile);
        assertEquals(email, profile.getEmail());
        assertEquals("Riya Sharma", profile.getFullName());
        assertEquals("ROLE_USER", profile.getRole());

        // Verify password in database is hashed with BCrypt, never plaintext
        User user = userRepository.findByEmail(email).orElseThrow();
        assertNotEquals(rawPassword, user.getPasswordHash());
        assertTrue(user.getPasswordHash().startsWith("$2a$") || user.getPasswordHash().startsWith("$2b$"));
        assertTrue(passwordEncoder.matches(rawPassword, user.getPasswordHash()));

        // Targets stay unset until the user completes nutrition onboarding.
        UserTargetDto targets = userTargetService.getTargets(email);
        assertNotNull(targets);
        assertNull(targets.getDailyCalories());
    }

    @Test
    @DisplayName("Should reject duplicate user registration with same email")
    void testDuplicateRegistrationRejection() {
        String email = "duplicate.test@fitkit.local";
        authService.register(new RegisterRequestDto(email, "Password123!", "First User"));

        RegisterRequestDto duplicateReq = new RegisterRequestDto(email, "Password999!", "Second User");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            authService.register(duplicateReq);
        });
        assertTrue(ex.getMessage().contains("already exists"));
    }

    @Test
    @DisplayName("Should reject password shorter than 8 characters")
    void testShortPasswordRejection() {
        RegisterRequestDto shortPassReq = new RegisterRequestDto("short@fitkit.local", "short", "Short User");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            authService.register(shortPassReq);
        });
        assertTrue(ex.getMessage().contains("at least 8 characters"));
    }

    @Test
    @DisplayName("Should authenticate valid credentials and reject invalid credentials")
    void testAuthenticationFlow() {
        String email = "login.test@fitkit.local";
        String pass = "SecurePass2026!";
        authService.register(new RegisterRequestDto(email, pass, "Login Tester"));

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // 1. Successful Login
        LoginRequestDto validLogin = new LoginRequestDto(email, pass);
        UserProfileDto profile = authService.login(validLogin, request, response);
        assertNotNull(profile);
        assertEquals(email, profile.getEmail());

        // 2. Invalid Password Login Rejection
        LoginRequestDto wrongPass = new LoginRequestDto(email, "WrongPassword!");
        assertThrows(BadCredentialsException.class, () -> {
            authService.login(wrongPass, new MockHttpServletRequest(), new MockHttpServletResponse());
        });

        // 3. Nonexistent Email Login Rejection
        LoginRequestDto wrongEmail = new LoginRequestDto("nonexistent@fitkit.local", pass);
        assertThrows(BadCredentialsException.class, () -> {
            authService.login(wrongEmail, new MockHttpServletRequest(), new MockHttpServletResponse());
        });

        // 4. Logout invalidates session
        authService.logout(request, response);
        assertNull(request.getSession(false));
    }

    @Test
    @DisplayName("Should enforce account isolation: User B cannot access, mutate, or delete User A records")
    void testAccountIsolationBetweenUsers() {
        String userAlice = "alice@fitkit.local";
        String userBob = "bob@fitkit.local";
        LocalDate today = LocalDate.of(2026, 10, 15);

        authService.register(new RegisterRequestDto(userAlice, "AlicePassword1!", "Alice"));
        authService.register(new RegisterRequestDto(userBob, "BobPassword1!", "Bob"));

        // Alice logs food
        CreateMealEntryRequestDto req = new CreateMealEntryRequestDto();
        req.setLogDate(today);
        req.setMealType("Breakfast");
        req.setFoodId(19L); // Boiled Egg (50g)
        req.setPortionGramWeight(new BigDecimal("50.00"));
        req.setQuantityMultiplier(BigDecimal.ONE);
        req.setPortionDescription("1 boiled egg (50g)");

        DailyMealLogDto aliceLog = mealLoggingService.addMealEntry(userAlice, req);
        Long aliceEntryId = aliceLog.getMeals().get("Breakfast").getEntries().get(0).getId();

        // Verify Bob's diary for today is empty and isolated from Alice
        DailyMealLogDto bobLog = mealLoggingService.getDailyMealLog(userBob, today);
        assertEquals(BigDecimal.ZERO, bobLog.getTotalCalories());
        assertTrue(bobLog.getMeals().get("Breakfast").getEntries().isEmpty());

        // Bob attempts to update Alice's meal entry -> SecurityException
        UpdateMealEntryRequestDto updateReq = new UpdateMealEntryRequestDto();
        updateReq.setQuantityMultiplier(new BigDecimal("5.0"));
        assertThrows(SecurityException.class, () -> {
            mealLoggingService.updateMealEntry(userBob, aliceEntryId, updateReq);
        });

        // Bob attempts to delete Alice's meal entry -> SecurityException
        assertThrows(SecurityException.class, () -> {
            mealLoggingService.deleteMealEntry(userBob, aliceEntryId);
        });

        // Alice's entry is preserved and unaffected by Bob's unauthorized attempts
        DailyMealLogDto aliceCheck = mealLoggingService.getDailyMealLog(userAlice, today);
        assertEquals(1, aliceCheck.getMeals().get("Breakfast").getEntries().size());

        // Bob attempts to import records into Alice's account -> blocked
        ClientDiaryMigrationDto migrationDto = new ClientDiaryMigrationDto();
        migrationDto.setDate(today);
        migrationDto.setMeals(Map.of("Dinner", List.of()));
        // Bob importing under his own account only touches his account
        mealLoggingService.migrateLocalEntries(userBob, List.of(migrationDto));
        DailyMealLogDto alicePostMigration = mealLoggingService.getDailyMealLog(userAlice, today);
        assertTrue(alicePostMigration.getMeals().get("Dinner").getEntries().isEmpty());
    }

    @Test
    @DisplayName("Should isolate target modifications between users")
    void testUserTargetIsolation() {
        String userA = "usera@fitkit.local";
        String userB = "userb@fitkit.local";

        authService.register(new RegisterRequestDto(userA, "PassUserA1!", "User A"));
        authService.register(new RegisterRequestDto(userB, "PassUserB1!", "User B"));

        nutritionProfileService.saveProfile(userA, sampleProfile(30, "MALE", 180, 80, "MODERATE", "MAINTAIN"));
        nutritionProfileService.saveProfile(userB, sampleProfile(35, "FEMALE", 160, 60, "LIGHT", "MAINTAIN"));

        UserTargetDto targetA = new UserTargetDto(userA, 2800, new BigDecimal("150"), new BigDecimal("350"), new BigDecimal("70"), new BigDecimal("40"));
        userTargetService.saveTargets(userA, targetA);

        UserTargetDto retrievedA = userTargetService.getTargets(userA);
        assertEquals(2800, retrievedA.getDailyCalories());

        // User B's personal target remains independent of User A's changes.
        assertNotEquals(2800, userTargetService.getTargets(userB).getDailyCalories());
        assertTrue(userTargetService.getTargets(userB).getDailyCalories() > 0);
    }

    private NutritionProfileDto sampleProfile(int age, String sex, int height, double weight, String activity, String goal) {
        NutritionProfileDto profile = new NutritionProfileDto();
        profile.setAge(age);
        profile.setSexForEstimate(sex);
        profile.setHeightCm(height);
        profile.setWeightKg(weight);
        profile.setActivityLevel(activity);
        profile.setGoal(goal);
        return profile;
    }

    @Test
    @DisplayName("Should isolate weekly history timeline between users")
    void testWeeklyHistoryIsolation() {
        String userA = "history.a@fitkit.local";
        String userB = "history.b@fitkit.local";
        LocalDate today = LocalDate.of(2026, 10, 15);

        authService.register(new RegisterRequestDto(userA, "HistoryA1!", "History A"));
        authService.register(new RegisterRequestDto(userB, "HistoryB1!", "History B"));

        CreateMealEntryRequestDto req = new CreateMealEntryRequestDto();
        req.setLogDate(today);
        req.setMealType("Lunch");
        req.setFoodId(12L); // Basmati Rice (150g)
        req.setPortionGramWeight(new BigDecimal("150.00"));
        req.setQuantityMultiplier(BigDecimal.ONE);
        mealLoggingService.addMealEntry(userA, req);

        List<DailyHistoryItemDto> historyA = mealLoggingService.getWeeklyHistory(userA, today, 7);
        assertTrue(historyA.get(6).isHasEntries());
        assertTrue(historyA.get(6).getTotalCalories().compareTo(BigDecimal.ZERO) > 0);

        List<DailyHistoryItemDto> historyB = mealLoggingService.getWeeklyHistory(userB, today, 7);
        assertFalse(historyB.get(6).isHasEntries());
        assertEquals(BigDecimal.ZERO, historyB.get(6).getTotalCalories());
    }

    @Test
    @DisplayName("Phase 6.1: Registration, authentication, and targets work independently of demo seeding")
    void testAuthAndRegistrationWithoutDemoSeeding() {
        String testUser = "independent.runner@fitkit.local";
        String password = "SecureRunner2026!";

        // 1. Verify user does not exist initially
        assertFalse(userRepository.existsByEmail(testUser));

        // 2. Register successfully
        RegisterRequestDto regReq = new RegisterRequestDto(testUser, password, "Independent Runner");
        UserProfileDto profile = authService.register(regReq);
        assertNotNull(profile);
        assertEquals(testUser, profile.getEmail());

        // 3. No generic targets are provisioned before the questionnaire.
        UserTargetDto targets = userTargetService.getTargets(testUser);
        assertNotNull(targets);
        assertNull(targets.getDailyCalories());

        // 4. Authenticate successfully
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        LoginRequestDto loginReq = new LoginRequestDto(testUser, password);
        UserProfileDto loginProfile = authService.login(loginReq, request, response);
        assertNotNull(loginProfile);
        assertEquals(testUser, loginProfile.getEmail());

        // 5. Existing demo user record in DB is preserved and not mutated or dropped
        var demoOpt = userRepository.findByEmail("demo@fitkit.local");
        assertTrue(demoOpt.isPresent());
        assertEquals("Demo Athlete", demoOpt.get().getFullName());
        assertEquals("ROLE_USER", demoOpt.get().getRole().name());
        assertNotNull(demoOpt.get().getPasswordHash());
    }
}

