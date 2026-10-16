package com.fitkit;

import com.fitkit.domain.entity.FoodItem;
import com.fitkit.domain.entity.FoodNutrient;
import com.fitkit.domain.entity.Recipe;
import com.fitkit.domain.entity.RecipeIngredient;
import com.fitkit.domain.enums.CookingMethod;
import com.fitkit.domain.enums.FoodState;
import com.fitkit.domain.enums.NutrientBasis;
import com.fitkit.domain.enums.UncertaintyRating;
import com.fitkit.dto.RecipeCalculationResultDto;
import com.fitkit.service.RecipeCalculationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RecipeCalculationTest {

    private RecipeCalculationService service;

    @BeforeEach
    void setUp() {
        service = new RecipeCalculationService();
    }

    private FoodItem createMockFood(String code, String name, BigDecimal kcal, BigDecimal protein, BigDecimal carbs, BigDecimal fat, BigDecimal fiber) {
        FoodItem item = new FoodItem();
        item.setFoodCode(code);
        item.setName(name);
        item.setFoodState(FoodState.RAW_INGREDIENT);

        FoodNutrient nut = new FoodNutrient();
        nut.setFood(item);
        nut.setBasis(NutrientBasis.PER_100G);
        nut.setEnergyKcal(kcal);
        nut.setProteinG(protein);
        nut.setAvailableCarbohydratesG(carbs);
        nut.setTotalFatG(fat);
        nut.setDietaryFiberG(fiber);

        item.setNutrients(List.of(nut));
        return item;
    }

    @Test
    @DisplayName("Should aggregate nutrients and apply cooking yield factor for Chapati")
    void testChapatiRecipeCalculation() {
        // Raw Whole Wheat Atta: 320.3 kcal, 10.57g protein, 61.20g carbs, 1.72g fat, 11.23g fiber per 100g
        FoodItem atta = createMockFood("IFCT_A019", "Whole Wheat Atta",
                new BigDecimal("320.30"), new BigDecimal("10.57"), new BigDecimal("61.20"),
                new BigDecimal("1.72"), new BigDecimal("11.23"));

        Recipe recipe = new Recipe();
        recipe.setRecipeCode("REC_CHAP_01");
        recipe.setTitle("Traditional Whole Wheat Chapati");
        recipe.setCookingMethod(CookingMethod.ROASTING_TAWA);
        recipe.setTotalRawWeightG(new BigDecimal("100.00"));
        recipe.setCookingYieldFactor(new BigDecimal("1.320")); // 100g dry flour -> 132g cooked chapatis
        recipe.setCookedWeightG(new BigDecimal("132.00"));
        recipe.setServingsYield(new BigDecimal("4.00")); // 4 chapatis
        recipe.setServingWeightG(new BigDecimal("33.00"));
        recipe.setUncertaintyRating(UncertaintyRating.LOW_STANDARDIZED);

        RecipeIngredient ing = new RecipeIngredient(recipe, atta, new BigDecimal("100.00"), "g", BigDecimal.ONE, "Atta flour");
        recipe.setIngredients(List.of(ing));

        RecipeCalculationResultDto result = service.calculateRecipe(recipe);

        assertThat(result).isNotNull();
        assertThat(result.getCookedWeightG()).isEqualByComparingTo("132.00");
        assertThat(result.getServingWeightG()).isEqualByComparingTo("33.00");

        // Per 100g cooked: 320.30 / 1.32 = 242.65 kcal
        assertThat(result.getEnergyKcalPer100g()).isEqualByComparingTo("242.65");
        // Protein per 100g: 10.57 / 1.32 = 8.01g
        assertThat(result.getProteinGPer100g()).isEqualByComparingTo("8.01");

        // Per serving (1 chapati of 33g): 320.30 / 4 = 80.08 kcal
        assertThat(result.getEnergyKcalPerServing()).isEqualByComparingTo("80.08");
        // Protein per serving: 10.57 / 4 = 2.64g
        assertThat(result.getProteinGPerServing()).isEqualByComparingTo("2.64");
    }

    @Test
    @DisplayName("Should account for added cooking fat in Dal Tadka recipe")
    void testDalTadkaCalculationWithAddedGhee() {
        // Raw Toor Dal: 330.8 kcal, 21.7g protein, 55.23g carbs, 1.56g fat, 9.06g fiber per 100g
        FoodItem dal = createMockFood("IFCT_B021", "Toor Dal",
                new BigDecimal("330.80"), new BigDecimal("21.70"), new BigDecimal("55.23"),
                new BigDecimal("1.56"), new BigDecimal("9.06"));

        // Ghee: 900.0 kcal, 0g protein, 0g carbs, 100g fat per 100g
        FoodItem ghee = createMockFood("IFCT_T013", "Ghee",
                new BigDecimal("900.00"), BigDecimal.ZERO, BigDecimal.ZERO,
                new BigDecimal("100.00"), BigDecimal.ZERO);

        Recipe recipe = new Recipe();
        recipe.setRecipeCode("REC_DAL_01");
        recipe.setTitle("Homestyle Dal Tadka");
        recipe.setCookingMethod(CookingMethod.PRESSURE_COOKING);
        recipe.setTotalRawWeightG(new BigDecimal("105.00")); // 100g dal + 5g ghee
        recipe.setCookingYieldFactor(new BigDecimal("3.048")); // 105g -> 320g cooked dal
        recipe.setCookedWeightG(new BigDecimal("320.00"));
        recipe.setServingsYield(new BigDecimal("2.13"));
        recipe.setServingWeightG(new BigDecimal("150.00")); // 150g per katori
        recipe.setUncertaintyRating(UncertaintyRating.MODERATE_HOME_VARIANCE);

        RecipeIngredient dalIng = new RecipeIngredient(recipe, dal, new BigDecimal("100.00"), "g", BigDecimal.ONE, "Dry pulse");
        RecipeIngredient gheeIng = new RecipeIngredient(recipe, ghee, new BigDecimal("5.00"), "g", BigDecimal.ONE, "Ghee tadka");

        recipe.setIngredients(List.of(dalIng, gheeIng));

        RecipeCalculationResultDto result = service.calculateRecipe(recipe);

        // Total batch calories: 330.80 + (5g * 9.0 = 45.0) = 375.80 kcal
        // Cooked weight: 105g * 3.048 = 320.04g -> ~320g
        // Calories per 100g cooked: 375.80 / 320.04 * 100 = 117.42 kcal
        assertThat(result.getEnergyKcalPer100g()).isGreaterThan(new BigDecimal("115.00"));
        assertThat(result.getEnergyKcalPer100g()).isLessThan(new BigDecimal("120.00"));

        // Total fat in batch: 1.56g (from dal) + 5.0g (from ghee) = 6.56g
        // Fat per 100g cooked: 6.56 / 320.04 * 100 ≈ 2.05g
        assertThat(result.getFatGPer100g()).isEqualByComparingTo("2.05");

        // Serving is ~150g (approx 176 kcal)
        assertThat(result.getEnergyKcalPerServing()).isGreaterThan(new BigDecimal("170.00"));
        assertThat(result.getEnergyKcalPerServing()).isLessThan(new BigDecimal("180.00"));
    }

    @Test
    @DisplayName("Should reject recipe with zero or negative ingredient quantities")
    void testRejectInvalidIngredientQuantity() {
        FoodItem atta = createMockFood("IFCT_A019", "Atta", new BigDecimal("320.0"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        Recipe recipe = new Recipe();
        recipe.setRecipeCode("REC_INVALID");
        recipe.setCookingYieldFactor(new BigDecimal("1.2"));
        recipe.setServingsYield(new BigDecimal("2.0"));

        RecipeIngredient invalidIng = new RecipeIngredient(recipe, atta, new BigDecimal("-10.00"), "g", BigDecimal.ONE, "Negative quantity");
        recipe.setIngredients(List.of(invalidIng));

        assertThatThrownBy(() -> service.calculateRecipe(recipe))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ingredient quantity must be positive");
    }

    @Test
    @DisplayName("Should reject recipe with non-positive cooking yield factor")
    void testRejectNonPositiveYieldFactor() {
        Recipe recipe = new Recipe();
        recipe.setRecipeCode("REC_INVALID_YIELD");
        recipe.setCookingYieldFactor(BigDecimal.ZERO); // Invalid
        recipe.setServingsYield(new BigDecimal("2.0"));
        recipe.setIngredients(new ArrayList<>());

        assertThatThrownBy(() -> service.calculateRecipe(recipe))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cooking yield factor must be positive");
    }
}
