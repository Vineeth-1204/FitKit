package com.fitkit;

import com.fitkit.domain.entity.FoodItem;
import com.fitkit.domain.entity.FoodNutrient;
import com.fitkit.domain.entity.Recipe;
import com.fitkit.domain.enums.FoodState;
import com.fitkit.domain.enums.NutrientBasis;
import com.fitkit.domain.enums.QualityStatus;
import com.fitkit.repository.FoodItemRepository;
import com.fitkit.repository.FoodNutrientRepository;
import com.fitkit.repository.RecipeRepository;
import com.fitkit.repository.SourceProvenanceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DatabaseConstraintsTest {

    @Autowired
    private FoodItemRepository foodRepo;

    @Autowired
    private FoodNutrientRepository nutrientRepo;

    @Autowired
    private RecipeRepository recipeRepo;

    @Autowired
    private SourceProvenanceRepository sourceRepo;

    @Test
    @DisplayName("Should verify that initial V1 and V2 migrations seeded provenances and catalog items")
    void testInitialSeedCatalogVerification() {
        assertThat(sourceRepo.count()).isGreaterThanOrEqualTo(4);
        assertThat(sourceRepo.existsById("IFCT_2017")).isTrue();
        assertThat(sourceRepo.existsById("USDA_FDC")).isTrue();
        assertThat(sourceRepo.existsById("OPEN_FOOD_FACTS")).isTrue();
        assertThat(sourceRepo.existsById("FITKIT_RECIPE_ENGINE")).isTrue();

        assertThat(foodRepo.count()).isGreaterThanOrEqualTo(11);
        assertThat(recipeRepo.count()).isGreaterThanOrEqualTo(3);
    }

    @Test
    @DisplayName("Should strictly separate RAW_INGREDIENT from COOKED_SINGLE and PREPARED_RECIPE")
    void testRawVsCookedSeparation() {
        List<FoodItem> rawFoods = foodRepo.findByFoodState(FoodState.RAW_INGREDIENT);
        List<FoodItem> cookedSingles = foodRepo.findByFoodState(FoodState.COOKED_SINGLE);
        List<FoodItem> recipes = foodRepo.findByFoodState(FoodState.PREPARED_RECIPE);

        assertThat(rawFoods).isNotEmpty();
        assertThat(cookedSingles).isNotEmpty();
        assertThat(recipes).isNotEmpty();

        // Verify White Rice raw vs cooked separation
        FoodItem rawRice = foodRepo.findByFoodCode("USDA_168878").orElseThrow();
        FoodItem cookedRice = foodRepo.findByFoodCode("USDA_2345388").orElseThrow();

        assertThat(rawRice.getFoodState()).isEqualTo(FoodState.RAW_INGREDIENT);
        assertThat(cookedRice.getFoodState()).isEqualTo(FoodState.COOKED_SINGLE);

        FoodNutrient rawNut = nutrientRepo.findByFoodIdAndBasis(rawRice.getId(), NutrientBasis.PER_100G).orElseThrow();
        FoodNutrient cookedNut = nutrientRepo.findByFoodIdAndBasis(cookedRice.getId(), NutrientBasis.PER_100G).orElseThrow();

        // Raw rice has ~365 kcal, cooked rice has ~130 kcal per 100g
        assertThat(rawNut.getEnergyKcal()).isEqualByComparingTo("365.00");
        assertThat(cookedNut.getEnergyKcal()).isEqualByComparingTo("130.00");
        assertThat(rawNut.getAvailableCarbohydratesG()).isEqualByComparingTo("79.95");
        assertThat(cookedNut.getAvailableCarbohydratesG()).isEqualByComparingTo("28.17");
    }

    @Test
    @DisplayName("Should verify Ghee has corrected 900.0 kcal energy and CORRECTED_ANOMALY status in database")
    void testGheeSeedNutrientAccuracy() {
        FoodItem ghee = foodRepo.findByFoodCode("IFCT_T013").orElseThrow();
        FoodNutrient nut = nutrientRepo.findByFoodIdAndBasis(ghee.getId(), NutrientBasis.PER_100G).orElseThrow();

        assertThat(nut.getEnergyKcal()).isEqualByComparingTo("900.00");
        assertThat(nut.getTotalFatG()).isEqualByComparingTo("100.00");
        assertThat(nut.getQualityStatus()).isEqualTo(QualityStatus.CORRECTED_ANOMALY);
        assertThat(nut.getCorrectionNotes()).contains("CORRECTION AUDIT");
    }

    @Test
    @DisplayName("Should verify seeded traditional recipes (Chapati, Idli, Dal Tadka)")
    void testSeededRecipes() {
        Recipe chapati = recipeRepo.findByRecipeCode("REC_CHAP_01").orElseThrow();
        assertThat(chapati.getTitle()).isEqualTo("Traditional Whole Wheat Chapati");
        assertThat(chapati.getCookingYieldFactor()).isEqualByComparingTo("1.320");
        assertThat(chapati.getIngredients()).hasSize(1);

        Recipe idli = recipeRepo.findByRecipeCode("REC_IDLI_01").orElseThrow();
        assertThat(idli.getTitle()).isEqualTo("Steamed Idli");
        assertThat(idli.getCookingYieldFactor()).isEqualByComparingTo("2.400");
        assertThat(idli.getIngredients()).hasSize(2);

        Recipe dalTadka = recipeRepo.findByRecipeCode("REC_DAL_01").orElseThrow();
        assertThat(dalTadka.getTitle()).isEqualTo("Homestyle Dal Tadka");
        assertThat(dalTadka.getIngredients()).hasSize(2); // Dal + Ghee
    }

    @Test
    @DisplayName("Should enforce uniqueness constraint on food_code")
    void testUniqueFoodCodeConstraint() {
        FoodItem duplicate = new FoodItem();
        duplicate.setFoodCode("IFCT_B021"); // Already exists
        duplicate.setSource(sourceRepo.findById("IFCT_2017").orElseThrow());
        duplicate.setName("Duplicate Toor Dal");
        duplicate.setFoodState(FoodState.RAW_INGREDIENT);

        assertThatThrownBy(() -> {
            foodRepo.saveAndFlush(duplicate);
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}
