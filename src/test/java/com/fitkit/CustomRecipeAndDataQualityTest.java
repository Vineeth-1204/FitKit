package com.fitkit;

import com.fitkit.domain.entity.FoodItem;
import com.fitkit.domain.entity.Recipe;
import com.fitkit.domain.enums.CookingMethod;
import com.fitkit.dto.CreateMealEntryRequestDto;
import com.fitkit.dto.CustomRecipeRequestDto;
import com.fitkit.dto.DailyMealLogDto;
import com.fitkit.dto.RecipeCalculationResultDto;
import com.fitkit.dto.RecipeDetailDto;
import com.fitkit.repository.FoodItemRepository;
import com.fitkit.repository.MealLogRepository;
import com.fitkit.repository.RecipeRepository;
import com.fitkit.service.CustomRecipeService;
import com.fitkit.service.MealLoggingService;
import com.fitkit.service.RecipeCalculationService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class CustomRecipeAndDataQualityTest {

    @Autowired
    private CustomRecipeService customRecipeService;

    @Autowired
    private RecipeCalculationService recipeCalculationService;

    @Autowired
    private MealLoggingService mealLoggingService;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private FoodItemRepository foodItemRepository;

    @Autowired
    private MealLogRepository mealLogRepository;

    private FoodItem rawAtta;
    private FoodItem toorDal;
    private FoodItem ghee;
    private FoodItem rawRice;
    private FoodItem uradDal;

    private static final String USER_A = "chef.alice@fitkit.local";
    private static final String USER_B = "taster.bob@fitkit.local";

    @BeforeEach
    public void setup() {
        rawAtta = foodItemRepository.findByFoodCode("IFCT_A019").orElseThrow();
        toorDal = foodItemRepository.findByFoodCode("IFCT_B021").orElseThrow();
        ghee = foodItemRepository.findByFoodCode("IFCT_T013").orElseThrow();
        rawRice = foodItemRepository.findByFoodCode("USDA_168878").orElseThrow();
        uradDal = foodItemRepository.findByFoodCode("IFCT_B003").orElseThrow();
    }

    @Test
    @DisplayName("Single-ingredient and multi-ingredient recipe calculations with correct yields")
    public void testRecipeCalculationsAndYield() {
        // Multi-ingredient: Homemade Dal Tadka Formulation (100g toor dal + 5g ghee, cooked to 320g, 2 servings)
        CustomRecipeRequestDto req = new CustomRecipeRequestDto();
        req.setTitle("Homemade Toor Dal");
        req.setCookingMethod("PRESSURE_COOKING");
        req.setCookedWeightG(new BigDecimal("320.0"));
        req.setServingsYield(new BigDecimal("2.0"));
        req.setIngredients(List.of(
                new CustomRecipeRequestDto.CustomIngredientItem(toorDal.getId(), new BigDecimal("100.0"), "g"),
                new CustomRecipeRequestDto.CustomIngredientItem(ghee.getId(), new BigDecimal("5.0"), "g")
        ));

        RecipeCalculationResultDto calc = customRecipeService.calculatePreview(req);

        assertNotNull(calc);
        assertEquals(new BigDecimal("105.0"), calc.getTotalRawWeightG());
        assertEquals(new BigDecimal("320.00"), calc.getCookedWeightG());
        assertEquals(new BigDecimal("2.0"), calc.getServingsCount());
        assertEquals(new BigDecimal("160.00"), calc.getServingWeightG());

        // Batch calories: 100g toor dal (330.8 kcal) + 5g ghee (45.0 kcal) = 375.8 kcal
        assertEquals(new BigDecimal("375.8"), calc.getTotalBatchCalories());

        // Per 100g cooked: 375.8 * 100 / 320 = 117.44 kcal
        assertEquals(new BigDecimal("117.44"), calc.getEnergyKcalPer100g());

        // Per serving: 375.8 / 2 = 187.90 kcal
        assertEquals(new BigDecimal("187.90"), calc.getEnergyKcalPerServing());
    }

    @Test
    @DisplayName("Yield handling does not apply yield twice when final cooked weight is provided")
    public void testYieldNotAppliedTwice() {
        CustomRecipeRequestDto req = new CustomRecipeRequestDto();
        req.setTitle("Measured Weight Rice");
        req.setCookedWeightG(new BigDecimal("300.00")); // User explicitly entered final batch weight
        req.setCookingYieldFactor(new BigDecimal("2.80")); // Even if slider was touched
        req.setServingsYield(new BigDecimal("2.0"));
        req.setIngredients(List.of(
                new CustomRecipeRequestDto.CustomIngredientItem(rawRice.getId(), new BigDecimal("100.0"), "g")
        ));

        RecipeDetailDto saved = customRecipeService.createCustomRecipe(USER_A, req);

        // Cooked weight must be exactly 300.00g (authoritative user measurement), NOT 300 * 2.8 = 840g!
        assertEquals(new BigDecimal("300.00"), saved.getCookedWeightG());
        assertEquals(new BigDecimal("150.00"), saved.getServingWeightG());
        // Derived yield factor: 300 / 100 = 3.0000
        assertEquals(0, new BigDecimal("3.000").compareTo(saved.getCookingYieldFactor()));
    }

    @Test
    @DisplayName("User A can create, retrieve, update, and soft-delete their own custom recipe")
    public void testCustomRecipeLifecycle() {
        CustomRecipeRequestDto req = new CustomRecipeRequestDto();
        req.setTitle("Alice's Protein Roti");
        req.setDescription("High-fiber whole wheat flatbread");
        req.setCookingMethod("ROASTING_TAWA");
        req.setCookingYieldFactor(new BigDecimal("1.30"));
        req.setServingsYield(new BigDecimal("4.0"));
        req.setIngredients(List.of(
                new CustomRecipeRequestDto.CustomIngredientItem(rawAtta.getId(), new BigDecimal("120.0"), "g")
        ));

        RecipeDetailDto created = customRecipeService.createCustomRecipe(USER_A, req);
        assertNotNull(created.getId());
        assertEquals("Alice's Protein Roti", created.getTitle());
        assertTrue(created.getIsCustom());
        assertEquals(USER_A, created.getUserId());

        // Retrieve by ID
        RecipeDetailDto retrieved = customRecipeService.getCustomRecipeById(USER_A, created.getId());
        assertEquals(created.getId(), retrieved.getId());

        // List for User A
        List<RecipeDetailDto> aliceRecipes = customRecipeService.getUserCustomRecipes(USER_A);
        assertEquals(1, aliceRecipes.size());
        assertEquals("Alice's Protein Roti", aliceRecipes.get(0).getTitle());

        // Update recipe
        req.setTitle("Alice's Super Protein Roti");
        req.setServingsYield(new BigDecimal("5.0"));
        RecipeDetailDto updated = customRecipeService.updateCustomRecipe(USER_A, created.getId(), req);
        assertEquals("Alice's Super Protein Roti", updated.getTitle());
        assertEquals(new BigDecimal("5.0"), updated.getServingsYield());

        // Soft delete
        customRecipeService.deleteCustomRecipe(USER_A, created.getId());
        List<RecipeDetailDto> afterDelete = customRecipeService.getUserCustomRecipes(USER_A);
        assertTrue(afterDelete.isEmpty());
    }

    @Test
    @DisplayName("User B cannot access, update, delete, or log User A's custom recipe")
    public void testCrossUserAccountIsolation() {
        // User A creates a recipe
        CustomRecipeRequestDto req = new CustomRecipeRequestDto();
        req.setTitle("Alice's Secret Stew");
        req.setServingsYield(new BigDecimal("2.0"));
        req.setIngredients(List.of(
                new CustomRecipeRequestDto.CustomIngredientItem(toorDal.getId(), new BigDecimal("100.0"), "g")
        ));

        RecipeDetailDto aliceRecipe = customRecipeService.createCustomRecipe(USER_A, req);
        Long recipeId = aliceRecipe.getId();

        // User B cannot view in list
        List<RecipeDetailDto> bobList = customRecipeService.getUserCustomRecipes(USER_B);
        assertTrue(bobList.isEmpty());

        // User B cannot get by ID
        assertThrows(SecurityException.class, () -> customRecipeService.getCustomRecipeById(USER_B, recipeId));

        // User B cannot update
        req.setTitle("Bob's Hijacked Stew");
        assertThrows(SecurityException.class, () -> customRecipeService.updateCustomRecipe(USER_B, recipeId, req));

        // User B cannot delete
        assertThrows(SecurityException.class, () -> customRecipeService.deleteCustomRecipe(USER_B, recipeId));

        // User B cannot log Alice's custom recipe into Bob's diary
        CreateMealEntryRequestDto logReq = new CreateMealEntryRequestDto();
        logReq.setMealType("Lunch");
        logReq.setRecipeId(recipeId);
        logReq.setLogDate(LocalDate.now());
        logReq.setPortionGramWeight(new BigDecimal("150.0"));
        logReq.setQuantityMultiplier(BigDecimal.ONE);

        assertThrows(SecurityException.class, () -> mealLoggingService.addMealEntry(USER_B, logReq));
    }

    @Test
    @DisplayName("Logging custom recipe produces accurate immutable snapshots that survive recipe updates")
    public void testMealLoggingAndSnapshotStability() {
        // User A creates a custom recipe
        CustomRecipeRequestDto req = new CustomRecipeRequestDto();
        req.setTitle("Alice's Dal");
        req.setCookedWeightG(new BigDecimal("300.0"));
        req.setServingsYield(new BigDecimal("2.0")); // 150g per serving
        req.setIngredients(List.of(
                new CustomRecipeRequestDto.CustomIngredientItem(toorDal.getId(), new BigDecimal("100.0"), "g")
        ));

        RecipeDetailDto recipe = customRecipeService.createCustomRecipe(USER_A, req);

        // User A logs 1 serving (150g) to Lunch
        LocalDate today = LocalDate.now();
        CreateMealEntryRequestDto logReq = new CreateMealEntryRequestDto();
        logReq.setMealType("Lunch");
        logReq.setRecipeId(recipe.getId());
        logReq.setLogDate(today);
        logReq.setPortionGramWeight(new BigDecimal("150.0"));
        logReq.setQuantityMultiplier(BigDecimal.ONE);

        DailyMealLogDto logDto = mealLoggingService.addMealEntry(USER_A, logReq);
        assertNotNull(logDto);

        // Verify snapshot values
        var lunchEntries = logDto.getMeals().get("Lunch").getEntries();
        assertEquals(1, lunchEntries.size());
        var entry = lunchEntries.get(0);
        assertEquals("Alice's Dal", entry.getFoodName());

        BigDecimal loggedCalories = entry.getEnergyKcal();
        // 100g toor dal = 330.8 kcal. Cooked to 300g (110.27 kcal/100g). 150g portion = 165.41 kcal
        assertEquals(new BigDecimal("165.41"), loggedCalories);

        // Now User A edits the custom recipe to have different ingredients / yield
        req.setTitle("Alice's Dal (Modified)");
        req.setCookedWeightG(new BigDecimal("600.0")); // Diluted with more water
        customRecipeService.updateCustomRecipe(USER_A, recipe.getId(), req);

        // Verify that the PREVIOUSLY logged meal entry snapshot remained completely unchanged!
        DailyMealLogDto refreshedLog = mealLoggingService.getDailyMealLog(USER_A, today);
        var refreshedEntry = refreshedLog.getMeals().get("Lunch").getEntries().get(0);
        assertEquals(loggedCalories, refreshedEntry.getEnergyKcal(), "Historical meal entry nutrient snapshot must remain stable when recipe is modified");
    }

    @Test
    @DisplayName("Validation rejects empty recipes, negative quantities, or invalid ingredients")
    public void testRecipeValidationConstraints() {
        // Empty ingredients
        CustomRecipeRequestDto emptyReq = new CustomRecipeRequestDto();
        emptyReq.setTitle("Empty Dish");
        emptyReq.setIngredients(List.of());
        assertThrows(IllegalArgumentException.class, () -> customRecipeService.createCustomRecipe(USER_A, emptyReq));

        // Negative quantity
        CustomRecipeRequestDto negativeQtyReq = new CustomRecipeRequestDto();
        negativeQtyReq.setTitle("Negative Dish");
        negativeQtyReq.setIngredients(List.of(
                new CustomRecipeRequestDto.CustomIngredientItem(toorDal.getId(), new BigDecimal("-50.0"), "g")
        ));
        assertThrows(IllegalArgumentException.class, () -> customRecipeService.createCustomRecipe(USER_A, negativeQtyReq));

        // Non-existent food item
        CustomRecipeRequestDto invalidFoodReq = new CustomRecipeRequestDto();
        invalidFoodReq.setTitle("Ghost Ingredient Dish");
        invalidFoodReq.setIngredients(List.of(
                new CustomRecipeRequestDto.CustomIngredientItem(999999L, new BigDecimal("100.0"), "g")
        ));
        assertThrows(IllegalArgumentException.class, () -> customRecipeService.createCustomRecipe(USER_A, invalidFoodReq));
    }

    @Test
    @DisplayName("Phase 6.1: Verify reconciled staple ingredients (A022, G017, D075, G011, G025, G026, T017) against IFCT 2017 authoritative records")
    public void testPhase61ReconciledStapleIngredients() {
        // 1. Semolina / Suji (IFCT A022)
        FoodItem semolina = foodItemRepository.findByFoodCode("IFCT_A022").orElseThrow();
        assertEquals("A022", semolina.getSourceRecordId());
        assertEquals("Triticum aestivum", semolina.getScientificName());
        var sNutr = semolina.getNutrients().get(0);
        assertEquals(new BigDecimal("333.70"), sNutr.getEnergyKcal());
        assertEquals(new BigDecimal("1396.00"), sNutr.getEnergyKj());
        assertEquals(new BigDecimal("11.38"), sNutr.getProteinG());
        assertEquals(new BigDecimal("68.43"), sNutr.getAvailableCarbohydratesG());
        assertEquals(new BigDecimal("0.80"), sNutr.getTotalFatG());
        assertEquals(new BigDecimal("9.72"), sNutr.getDietaryFiberG());
        assertEquals("SOURCE_VERIFIED", sNutr.getQualityStatus().name());

        // Atwater energy check: 11.38*4 + 68.43*4 + 0.80*9 = 326.44 kcal vs source 333.70 kcal
        BigDecimal sAtwater = sNutr.getProteinG().multiply(new BigDecimal("4"))
                .add(sNutr.getAvailableCarbohydratesG().multiply(new BigDecimal("4")))
                .add(sNutr.getTotalFatG().multiply(new BigDecimal("9")));
        assertEquals(new BigDecimal("326.44"), sAtwater);

        // 2. Fresh Onion (IFCT G017)
        FoodItem onion = foodItemRepository.findByFoodCode("IFCT_G017").orElseThrow();
        assertEquals("G017", onion.getSourceRecordId());
        var oNutr = onion.getNutrients().get(0);
        assertEquals(new BigDecimal("48.50"), oNutr.getEnergyKcal());
        assertEquals(new BigDecimal("203.00"), oNutr.getEnergyKj());
        assertEquals(new BigDecimal("1.20"), oNutr.getProteinG());
        assertEquals(new BigDecimal("9.30"), oNutr.getAvailableCarbohydratesG());
        assertEquals(new BigDecimal("0.40"), oNutr.getTotalFatG());
        assertEquals(new BigDecimal("2.50"), oNutr.getDietaryFiberG());

        // 3. Fresh Tomato (IFCT D075)
        FoodItem tomato = foodItemRepository.findByFoodCode("IFCT_D075").orElseThrow();
        assertEquals("D075", tomato.getSourceRecordId());
        var tNutr = tomato.getNutrients().get(0);
        assertEquals(new BigDecimal("20.80"), tNutr.getEnergyKcal());
        assertEquals(new BigDecimal("87.00"), tNutr.getEnergyKj());
        assertEquals(new BigDecimal("0.90"), tNutr.getProteinG());
        assertEquals(new BigDecimal("3.60"), tNutr.getAvailableCarbohydratesG());
        assertEquals(new BigDecimal("0.20"), tNutr.getTotalFatG());

        // 4. Green Chili (IFCT G011)
        FoodItem chili = foodItemRepository.findByFoodCode("IFCT_G011").orElseThrow();
        assertEquals("G011", chili.getSourceRecordId());
        var cNutr = chili.getNutrients().get(0);
        assertEquals(new BigDecimal("45.70"), cNutr.getEnergyKcal());
        assertEquals(new BigDecimal("191.00"), cNutr.getEnergyKj());
        assertEquals(new BigDecimal("2.60"), cNutr.getProteinG());
        assertEquals(new BigDecimal("6.80"), cNutr.getAvailableCarbohydratesG());

        // 5. Cumin Seeds (IFCT G025)
        FoodItem cumin = foodItemRepository.findByFoodCode("IFCT_G025").orElseThrow();
        assertEquals("G025", cumin.getSourceRecordId());
        var cuNutr = cumin.getNutrients().get(0);
        assertEquals(new BigDecimal("304.50"), cuNutr.getEnergyKcal());
        assertEquals(new BigDecimal("1274.00"), cuNutr.getEnergyKj());
        assertEquals(new BigDecimal("18.70"), cuNutr.getProteinG());
        assertEquals(new BigDecimal("36.60"), cuNutr.getAvailableCarbohydratesG());
        assertEquals(new BigDecimal("15.00"), cuNutr.getTotalFatG());
        assertEquals(new BigDecimal("22.62"), cuNutr.getDietaryFiberG());

        // 6. Turmeric Powder (IFCT G026)
        FoodItem turmeric = foodItemRepository.findByFoodCode("IFCT_G026").orElseThrow();
        assertEquals("G026", turmeric.getSourceRecordId());
        var tuNutr = turmeric.getNutrients().get(0);
        assertEquals(new BigDecimal("280.60"), tuNutr.getEnergyKcal());
        assertEquals(new BigDecimal("1174.00"), tuNutr.getEnergyKj());
        assertEquals(new BigDecimal("6.30"), tuNutr.getProteinG());
        assertEquals(new BigDecimal("64.90"), tuNutr.getAvailableCarbohydratesG());
        assertEquals(new BigDecimal("3.20"), tuNutr.getTotalFatG());
        assertEquals(new BigDecimal("21.10"), tuNutr.getDietaryFiberG());

        // 7. Mustard Oil (IFCT T017)
        FoodItem mustardOil = foodItemRepository.findByFoodCode("IFCT_T017").orElseThrow();
        assertEquals("T017", mustardOil.getSourceRecordId());
        var mNutr = mustardOil.getNutrients().get(0);
        assertEquals(new BigDecimal("884.00"), mNutr.getEnergyKcal());
        assertEquals(new BigDecimal("3700.00"), mNutr.getEnergyKj());
        assertEquals(new BigDecimal("0.00"), mNutr.getProteinG());
        assertEquals(new BigDecimal("0.00"), mNutr.getAvailableCarbohydratesG());
        assertEquals(new BigDecimal("100.00"), mNutr.getTotalFatG());
        assertEquals("0 kJ (anomaly)", mNutr.getRawSourceEnergy());
        assertEquals("CORRECTED_ANOMALY", mNutr.getQualityStatus().name());

        // Verify retired codes no longer exist
        assertTrue(foodItemRepository.findByFoodCode("IFCT_A018").isEmpty());
        assertTrue(foodItemRepository.findByFoodCode("IFCT_C012").isEmpty());
        assertTrue(foodItemRepository.findByFoodCode("IFCT_C035").isEmpty());
        assertTrue(foodItemRepository.findByFoodCode("IFCT_D005").isEmpty());
        assertTrue(foodItemRepository.findByFoodCode("IFCT_D004").isEmpty());
        assertTrue(foodItemRepository.findByFoodCode("IFCT_D028").isEmpty());
    }

    @Test
    @DisplayName("Phase 6.1: Custom recipe calculation using reconciled staple ingredients (Vegetable Upma)")
    public void testCustomRecipeCalculationWithReconciledStaples() {
        FoodItem semolina = foodItemRepository.findByFoodCode("IFCT_A022").orElseThrow();
        FoodItem onion = foodItemRepository.findByFoodCode("IFCT_G017").orElseThrow();
        FoodItem mustardOil = foodItemRepository.findByFoodCode("IFCT_T017").orElseThrow();
        FoodItem cumin = foodItemRepository.findByFoodCode("IFCT_G025").orElseThrow();

        // 100g semolina (333.7 kcal) + 50g onion (24.25 kcal) + 5g mustard oil (44.2 kcal) + 2g cumin (6.09 kcal)
        // Total raw = 157.0g. Expected total batch calories = 333.7 + 24.25 + 44.2 + 6.09 = 408.24 kcal (~408.2 kcal)
        CustomRecipeRequestDto req = new CustomRecipeRequestDto();
        req.setTitle("Homemade Suji Upma");
        req.setCookingMethod("BOILING");
        req.setCookedWeightG(new BigDecimal("350.0")); // Cooked with water to 350g
        req.setServingsYield(new BigDecimal("2.0"));
        req.setIngredients(List.of(
                new CustomRecipeRequestDto.CustomIngredientItem(semolina.getId(), new BigDecimal("100.0"), "g"),
                new CustomRecipeRequestDto.CustomIngredientItem(onion.getId(), new BigDecimal("50.0"), "g"),
                new CustomRecipeRequestDto.CustomIngredientItem(mustardOil.getId(), new BigDecimal("5.0"), "g"),
                new CustomRecipeRequestDto.CustomIngredientItem(cumin.getId(), new BigDecimal("2.0"), "g")
        ));

        RecipeCalculationResultDto calc = customRecipeService.calculatePreview(req);
        assertNotNull(calc);
        assertEquals(new BigDecimal("157.0"), calc.getTotalRawWeightG());
        assertEquals(new BigDecimal("350.00"), calc.getCookedWeightG());
        assertEquals(new BigDecimal("2.0"), calc.getServingsCount());
        assertEquals(new BigDecimal("175.00"), calc.getServingWeightG());

        // Batch calories check: 408.2 kcal
        assertEquals(new BigDecimal("408.2"), calc.getTotalBatchCalories());
        // Per 100g cooked: 408.24 * 100 / 350 = 116.64 kcal
        assertEquals(new BigDecimal("116.64"), calc.getEnergyKcalPer100g());
        // Per serving: 408.24 / 2 = 204.12 kcal
        assertEquals(new BigDecimal("204.12"), calc.getEnergyKcalPerServing());
    }
}

