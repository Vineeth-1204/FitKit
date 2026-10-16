package com.fitkit;

import com.fitkit.dto.*;
import com.fitkit.service.MealLoggingService;
import com.fitkit.service.UserTargetService;
import com.fitkit.service.NutritionProfileService;
import com.fitkit.service.AuthService;
import com.fitkit.dto.RegisterRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class MealLoggingIntegrationTest {

    @Autowired
    private MealLoggingService mealLoggingService;

    @Autowired
    private UserTargetService userTargetService;
    @Autowired
    private NutritionProfileService nutritionProfileService;
    @Autowired
    private AuthService authService;

    @Test
    @DisplayName("Should persist and retrieve user daily nutrition targets")
    void testUserTargetPersistence() {
        String userId = "test-user-1";
        authService.register(new RegisterRequestDto(userId, "StrongPassword1!", "Test User"));
        NutritionProfileDto profile = new NutritionProfileDto();
        profile.setAge(30);
        profile.setSexForEstimate("UNSPECIFIED");
        profile.setHeightCm(170);
        profile.setWeightKg(70.0);
        profile.setActivityLevel("MODERATE");
        profile.setGoal("MAINTAIN");
        nutritionProfileService.saveProfile(userId, profile);
        UserTargetDto initial = userTargetService.getTargets(userId);
        assertNotNull(initial);
        assertTrue(initial.getDailyCalories() > 0);

        UserTargetDto custom = new UserTargetDto(
                userId,
                2400,
                new BigDecimal("120.0"),
                new BigDecimal("280.0"),
                new BigDecimal("70.0"),
                new BigDecimal("35.0")
        );

        UserTargetDto saved = userTargetService.saveTargets(userId, custom);
        assertEquals(2400, saved.getDailyCalories());
        assertEquals(new BigDecimal("120.0"), saved.getProteinG());

        UserTargetDto retrieved = userTargetService.getTargets(userId);
        assertEquals(2400, retrieved.getDailyCalories());
        assertEquals(new BigDecimal("120.0"), retrieved.getProteinG());
    }

    @Test
    @DisplayName("Should log food entry with immutable nutrient snapshot and accurate daily totals")
    void testMealLoggingAndTotals() {
        String userId = "test-user-2";
        LocalDate today = LocalDate.of(2026, 10, 15);

        // Food 12: Cooked Basmati Rice (121.0 kcal, 2.54g P, 26.8g C, 0.38g F per 100g)
        // Portion: 150g, qty: 1.0 -> 150g total weight.
        // Expected calories: 121.0 * 1.50 = 181.50 -> 182 kcal.
        CreateMealEntryRequestDto req1 = new CreateMealEntryRequestDto();
        req1.setLogDate(today);
        req1.setMealType("Lunch");
        req1.setFoodId(12L);
        req1.setPortionGramWeight(new BigDecimal("150.00"));
        req1.setPortionDescription("1 medium katori (150g)");
        req1.setQuantityMultiplier(BigDecimal.ONE);

        DailyMealLogDto logAfter1 = mealLoggingService.addMealEntry(userId, req1);
        assertNotNull(logAfter1);
        assertEquals(today, logAfter1.getDate());
        assertTrue(logAfter1.getTotalCalories().compareTo(new BigDecimal("182")) == 0 ||
                   logAfter1.getTotalCalories().compareTo(new BigDecimal("181")) == 0);

        MealSectionDto lunchSection = logAfter1.getMeals().get("Lunch");
        assertNotNull(lunchSection);
        assertEquals(1, lunchSection.getEntries().size());

        MealEntryDto entry1 = lunchSection.getEntries().get(0);
        assertEquals("Basmati Rice, cooked", entry1.getFoodName());
        assertEquals(new BigDecimal("150.00"), entry1.getTotalGramWeight());
        assertNotNull(entry1.getEnergyKcal());
        assertNotNull(entry1.getProteinG());

        // Food 19: Boiled Whole Chicken Egg (155 kcal, 12.58g P, 1.12g C, 10.61g F per 100g)
        // Portion: 50g (1 egg), qty: 2.0 -> 100g total weight
        CreateMealEntryRequestDto req2 = new CreateMealEntryRequestDto();
        req2.setLogDate(today);
        req2.setMealType("Breakfast");
        req2.setFoodId(19L);
        req2.setPortionGramWeight(new BigDecimal("50.00"));
        req2.setPortionDescription("1 large boiled egg (50g)");
        req2.setQuantityMultiplier(new BigDecimal("2.0"));

        DailyMealLogDto logAfter2 = mealLoggingService.addMealEntry(userId, req2);
        MealSectionDto bfastSection = logAfter2.getMeals().get("Breakfast");
        assertEquals(1, bfastSection.getEntries().size());
        assertEquals(new BigDecimal("155.00"), bfastSection.getEntries().get(0).getEnergyKcal());

        // Daily total should equal Breakfast + Lunch
        BigDecimal expectedDaily = bfastSection.getSubtotalCalories().add(lunchSection.getSubtotalCalories());
        assertEquals(expectedDaily.setScale(0, BigDecimal.ROUND_HALF_UP), logAfter2.getTotalCalories());
    }

    @Test
    @DisplayName("Should update meal entry quantity multiplier and recalculate nutrient snapshot")
    void testUpdateMealEntryQuantity() {
        String userId = "test-user-3";
        LocalDate today = LocalDate.of(2026, 10, 15);

        // Add 1 egg (50g = 77.5 kcal)
        CreateMealEntryRequestDto req = new CreateMealEntryRequestDto();
        req.setLogDate(today);
        req.setMealType("Breakfast");
        req.setFoodId(19L);
        req.setPortionGramWeight(new BigDecimal("50.00"));
        req.setPortionDescription("1 large boiled egg");
        req.setQuantityMultiplier(BigDecimal.ONE);

        DailyMealLogDto log = mealLoggingService.addMealEntry(userId, req);
        Long entryId = log.getMeals().get("Breakfast").getEntries().get(0).getId();

        // Update to 3 eggs (qty 3.0)
        UpdateMealEntryRequestDto updateReq = new UpdateMealEntryRequestDto();
        updateReq.setQuantityMultiplier(new BigDecimal("3.0"));
        updateReq.setPortionGramWeight(new BigDecimal("50.00"));

        DailyMealLogDto updatedLog = mealLoggingService.updateMealEntry(userId, entryId, updateReq);
        MealEntryDto updatedEntry = updatedLog.getMeals().get("Breakfast").getEntries().get(0);

        assertEquals(new BigDecimal("150.00"), updatedEntry.getTotalGramWeight());
        // 155 kcal per 100g * 1.5 = 232.50 kcal
        assertEquals(new BigDecimal("232.50"), updatedEntry.getEnergyKcal());
    }

    @Test
    @DisplayName("Should delete meal entry and immediately reduce daily and meal totals")
    void testDeleteMealEntry() {
        String userId = "test-user-4";
        LocalDate today = LocalDate.of(2026, 10, 15);

        CreateMealEntryRequestDto req = new CreateMealEntryRequestDto();
        req.setLogDate(today);
        req.setMealType("Snacks");
        req.setFoodId(24L); // Banana (89 kcal per 100g)
        req.setPortionGramWeight(new BigDecimal("100.00"));
        req.setQuantityMultiplier(BigDecimal.ONE);

        DailyMealLogDto log = mealLoggingService.addMealEntry(userId, req);
        assertEquals(new BigDecimal("89"), log.getTotalCalories());
        Long entryId = log.getMeals().get("Snacks").getEntries().get(0).getId();

        DailyMealLogDto logAfterDelete = mealLoggingService.deleteMealEntry(userId, entryId);
        assertEquals(new BigDecimal("0"), logAfterDelete.getTotalCalories());
        assertTrue(logAfterDelete.getMeals().get("Snacks").getEntries().isEmpty());
    }

    @Test
    @DisplayName("Should prevent cross-user deletion access")
    void testCrossUserAccessControl() {
        String userA = "user-alice";
        String userB = "user-bob";
        LocalDate today = LocalDate.of(2026, 10, 15);

        CreateMealEntryRequestDto req = new CreateMealEntryRequestDto();
        req.setLogDate(today);
        req.setMealType("Dinner");
        req.setFoodId(20L); // Chicken breast
        req.setPortionGramWeight(new BigDecimal("100.00"));
        req.setQuantityMultiplier(BigDecimal.ONE);

        DailyMealLogDto log = mealLoggingService.addMealEntry(userA, req);
        Long entryId = log.getMeals().get("Dinner").getEntries().get(0).getId();

        // User Bob attempts to delete User Alice's entry
        assertThrows(SecurityException.class, () -> {
            mealLoggingService.deleteMealEntry(userB, entryId);
        });
    }

    @Test
    @DisplayName("Should retrieve 7-day historical timeline with empty days preserved as zero")
    void testWeeklyHistory() {
        String userId = "test-user-history";
        LocalDate today = LocalDate.of(2026, 10, 15);

        // Log an entry only on today
        CreateMealEntryRequestDto req = new CreateMealEntryRequestDto();
        req.setLogDate(today);
        req.setMealType("Breakfast");
        req.setFoodId(19L);
        req.setPortionGramWeight(new BigDecimal("50.00"));
        req.setQuantityMultiplier(BigDecimal.ONE);
        mealLoggingService.addMealEntry(userId, req);

        List<DailyHistoryItemDto> history = mealLoggingService.getWeeklyHistory(userId, today, 7);
        assertEquals(7, history.size());

        // The last item is today
        DailyHistoryItemDto todayItem = history.get(6);
        assertEquals(today, todayItem.getDate());
        assertTrue(todayItem.isHasEntries());
        assertTrue(todayItem.getTotalCalories().compareTo(BigDecimal.ZERO) > 0);

        // Days before today should have hasEntries = false and totalCalories = 0
        DailyHistoryItemDto prevItem = history.get(5);
        assertFalse(prevItem.isHasEntries());
        assertEquals(BigDecimal.ZERO, prevItem.getTotalCalories());
    }

    @Test
    @DisplayName("Should safely migrate browser localStorage entries into database")
    void testLocalStorageMigration() {
        String userId = "test-user-migrate";
        LocalDate date = LocalDate.of(2026, 10, 14);

        ClientDiaryMigrationDto.MigratedItem item = new ClientDiaryMigrationDto.MigratedItem();
        item.setId("local-1");
        item.setName("Chapati");
        item.setPortionDesc("2 medium (70g)");
        item.setGramWeight(new BigDecimal("70.00"));
        item.setCalories(new BigDecimal("170.00"));
        item.setProtein(new BigDecimal("5.6"));
        item.setCarbs(new BigDecimal("32.4"));
        item.setFat(new BigDecimal("1.0"));
        item.setFiber(new BigDecimal("5.8"));
        item.setState("COOKED_SINGLE");

        ClientDiaryMigrationDto diaryDto = new ClientDiaryMigrationDto();
        diaryDto.setDate(date);
        diaryDto.setMeals(Map.of("Breakfast", List.of(item)));

        int count = mealLoggingService.migrateLocalEntries(userId, List.of(diaryDto));
        assertEquals(1, count);

        // Verify it now exists in database
        DailyMealLogDto log = mealLoggingService.getDailyMealLog(userId, date);
        assertNotNull(log);
        assertEquals(1, log.getMeals().get("Breakfast").getEntries().size());
        assertEquals("Chapati", log.getMeals().get("Breakfast").getEntries().get(0).getFoodName());

        // Second migration of same date should be safely skipped (idempotent)
        int secondCount = mealLoggingService.migrateLocalEntries(userId, List.of(diaryDto));
        assertEquals(0, secondCount);
    }

    @Test
    @DisplayName("Should support distinct date navigation and independent data loading across days")
    void testDateNavigationAndDataLoading() {
        String userId = "test-user-date-nav";
        LocalDate dateA = LocalDate.of(2026, 10, 10);
        LocalDate dateB = LocalDate.of(2026, 10, 12);

        // 1. Log Breakfast on Date A
        CreateMealEntryRequestDto reqA = new CreateMealEntryRequestDto();
        reqA.setLogDate(dateA);
        reqA.setMealType("Breakfast");
        reqA.setFoodId(19L); // Boiled egg
        reqA.setPortionGramWeight(new BigDecimal("50.00"));
        reqA.setPortionDescription("1 egg (50g)");
        reqA.setQuantityMultiplier(BigDecimal.ONE);
        mealLoggingService.addMealEntry(userId, reqA);

        // 2. Log Lunch on Date B
        CreateMealEntryRequestDto reqB = new CreateMealEntryRequestDto();
        reqB.setLogDate(dateB);
        reqB.setMealType("Lunch");
        reqB.setFoodId(12L); // Cooked basmati rice
        reqB.setPortionGramWeight(new BigDecimal("150.00"));
        reqB.setPortionDescription("150g rice");
        reqB.setQuantityMultiplier(BigDecimal.ONE);
        mealLoggingService.addMealEntry(userId, reqB);

        // 3. Navigate to Date A: should strictly contain Date A's breakfast and no lunch
        DailyMealLogDto logA = mealLoggingService.getDailyMealLog(userId, dateA);
        assertNotNull(logA);
        assertEquals(dateA, logA.getDate());
        assertEquals(1, logA.getMeals().get("Breakfast").getEntries().size());
        assertEquals(0, logA.getMeals().get("Lunch").getEntries().size());
        assertTrue(logA.getTotalCalories().compareTo(BigDecimal.ZERO) > 0);

        // 4. Navigate to Date B: should strictly contain Date B's lunch and no breakfast
        DailyMealLogDto logB = mealLoggingService.getDailyMealLog(userId, dateB);
        assertNotNull(logB);
        assertEquals(dateB, logB.getDate());
        assertEquals(0, logB.getMeals().get("Breakfast").getEntries().size());
        assertEquals(1, logB.getMeals().get("Lunch").getEntries().size());
        assertTrue(logB.getTotalCalories().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("Should return valid empty-day state when navigating to unlogged dates, then support logging")
    void testEmptyDayStateAndLoading() {
        String userId = "test-user-empty-day";
        LocalDate unloggedDate = LocalDate.of(2026, 10, 11);

        // 1. Loading an unlogged date should yield clean empty state: 0 calories, all 4 meal sections present and empty
        DailyMealLogDto emptyLog = mealLoggingService.getDailyMealLog(userId, unloggedDate);
        assertNotNull(emptyLog);
        assertEquals(unloggedDate, emptyLog.getDate());
        assertEquals(BigDecimal.ZERO, emptyLog.getTotalCalories());
        assertNotNull(emptyLog.getMeals());
        assertTrue(emptyLog.getMeals().containsKey("Breakfast"));
        assertTrue(emptyLog.getMeals().containsKey("Lunch"));
        assertTrue(emptyLog.getMeals().containsKey("Dinner"));
        assertTrue(emptyLog.getMeals().containsKey("Snacks"));
        assertTrue(emptyLog.getMeals().get("Breakfast").getEntries().isEmpty());
        assertTrue(emptyLog.getMeals().get("Lunch").getEntries().isEmpty());
        assertTrue(emptyLog.getMeals().get("Dinner").getEntries().isEmpty());
        assertTrue(emptyLog.getMeals().get("Snacks").getEntries().isEmpty());

        // 2. Logging a meal for that date should transition the date from empty to populated
        CreateMealEntryRequestDto req = new CreateMealEntryRequestDto();
        req.setLogDate(unloggedDate);
        req.setMealType("Snacks");
        req.setFoodId(24L); // Banana
        req.setPortionGramWeight(new BigDecimal("100.00"));
        req.setPortionDescription("1 medium banana");
        req.setQuantityMultiplier(BigDecimal.ONE);
        DailyMealLogDto populatedLog = mealLoggingService.addMealEntry(userId, req);

        assertNotNull(populatedLog);
        assertEquals(unloggedDate, populatedLog.getDate());
        assertEquals(new BigDecimal("89"), populatedLog.getTotalCalories());
        assertEquals(1, populatedLog.getMeals().get("Snacks").getEntries().size());
    }
}
