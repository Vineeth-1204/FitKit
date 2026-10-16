package com.fitkit.runner;

import com.fitkit.domain.entity.FoodItem;
import com.fitkit.domain.entity.Recipe;
import com.fitkit.dto.RecipeCalculationResultDto;
import com.fitkit.repository.FoodItemRepository;
import com.fitkit.repository.FoodNutrientRepository;
import com.fitkit.repository.RecipeRepository;
import com.fitkit.service.RecipeCalculationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Component
@Profile("!test")
public class DataImportRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataImportRunner.class);

    private final FoodItemRepository foodRepo;
    private final FoodNutrientRepository nutrientRepo;
    private final RecipeRepository recipeRepo;
    private final RecipeCalculationService recipeService;

    public DataImportRunner(FoodItemRepository foodRepo,
                            FoodNutrientRepository nutrientRepo,
                            RecipeRepository recipeRepo,
                            RecipeCalculationService recipeService) {
        this.foodRepo = foodRepo;
        this.nutrientRepo = nutrientRepo;
        this.recipeRepo = recipeRepo;
        this.recipeService = recipeService;
    }

    @Override
    @Transactional(readOnly = true)
    public void run(String... args) {
        log.info("==============================================================================");
        log.info("FitKit Nutrition Data Foundation Initialized");
        log.info("Total Food Items in Catalog: {}", foodRepo.count());
        log.info("Total Nutrient Profiles: {}", nutrientRepo.count());
        log.info("Total Documented Recipes: {}", recipeRepo.count());
        log.info("==============================================================================");

        List<FoodItem> items = foodRepo.findAll();
        for (FoodItem f : items) {
            log.info(" - [{}] {} ({}) | State: {}", f.getFoodCode(), f.getName(), f.getSource().getId(), f.getFoodState());
        }

        List<Recipe> recipes = recipeRepo.findAll();
        for (Recipe r : recipes) {
            try {
                RecipeCalculationResultDto calc = recipeService.calculateRecipe(r);
                log.info(" Recipe Calculation [{}] {}: Cooked Weight={}g, Per 100g: {} kcal, Per Serving: {} kcal ({}g)",
                        r.getRecipeCode(), r.getTitle(), calc.getCookedWeightG(),
                        calc.getEnergyKcalPer100g(), calc.getEnergyKcalPerServing(), calc.getServingWeightG());
            } catch (Exception e) {
                log.warn(" Recipe calculation error for {}: {}", r.getRecipeCode(), e.getMessage());
            }
        }
        log.info("==============================================================================");
    }
}
