package com.fitkit.controller;

import com.fitkit.domain.entity.FoodItem;
import com.fitkit.domain.entity.FoodNutrient;
import com.fitkit.domain.entity.Recipe;
import com.fitkit.domain.entity.RecipeIngredient;
import com.fitkit.domain.enums.CookingMethod;
import com.fitkit.domain.enums.NutrientBasis;
import com.fitkit.domain.enums.UncertaintyRating;
import com.fitkit.dto.CustomRecipeRequestDto;
import com.fitkit.dto.RecipeCalculationResultDto;
import com.fitkit.dto.RecipeDetailDto;
import com.fitkit.dto.RecipeIngredientDto;
import com.fitkit.repository.FoodItemRepository;
import com.fitkit.repository.RecipeRepository;
import com.fitkit.service.RecipeCalculationService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recipes")
@CrossOrigin(origins = "*")
public class RecipeController {

    private final RecipeRepository recipeRepository;
    private final FoodItemRepository foodItemRepository;
    private final RecipeCalculationService recipeCalculationService;

    public RecipeController(RecipeRepository recipeRepository,
                            FoodItemRepository foodItemRepository,
                            RecipeCalculationService recipeCalculationService) {
        this.recipeRepository = recipeRepository;
        this.foodItemRepository = foodItemRepository;
        this.recipeCalculationService = recipeCalculationService;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<RecipeDetailDto>> getAllRecipes() {
        List<Recipe> recipes = recipeRepository.findAllWithIngredients();
        List<RecipeDetailDto> result = recipes.stream()
                .map(this::mapToRecipeDetailDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<RecipeDetailDto> getRecipeById(@PathVariable Long id) {
        return recipeRepository.findByIdWithIngredients(id)
                .map(this::mapToRecipeDetailDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/calculate")
    @Transactional(readOnly = true)
    public ResponseEntity<RecipeCalculationResultDto> recalculateRecipe(
            @PathVariable Long id,
            @RequestParam(required = false) BigDecimal yieldFactor,
            @RequestParam(required = false) BigDecimal servings) {

        return recipeRepository.findByIdWithIngredients(id)
                .map(recipe -> {
                    if (yieldFactor != null && yieldFactor.compareTo(BigDecimal.ZERO) > 0) {
                        recipe.setCookingYieldFactor(yieldFactor);
                        recipe.setCookedWeightG(recipe.getTotalRawWeightG().multiply(yieldFactor).setScale(2, RoundingMode.HALF_UP));
                    }
                    if (servings != null && servings.compareTo(BigDecimal.ZERO) > 0) {
                        recipe.setServingsYield(servings);
                        recipe.setServingWeightG(recipe.getCookedWeightG().divide(servings, 2, RoundingMode.HALF_UP));
                    }
                    return ResponseEntity.ok(recipeCalculationService.calculateRecipe(recipe));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/custom-calculate")
    @Transactional(readOnly = true)
    public ResponseEntity<RecipeCalculationResultDto> calculateCustomRecipe(
            @RequestBody CustomRecipeRequestDto request) {

        if (request.getIngredients() == null || request.getIngredients().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Recipe recipe = new Recipe();
        recipe.setRecipeCode("CUSTOM_" + System.currentTimeMillis());
        recipe.setTitle(request.getTitle() != null && !request.getTitle().isEmpty() ? request.getTitle() : "Custom User Dish");
        recipe.setCuisineRegion("Custom Composition");

        BigDecimal yieldFactor = request.getCookingYieldFactor() != null && request.getCookingYieldFactor().compareTo(BigDecimal.ZERO) > 0
                ? request.getCookingYieldFactor() : BigDecimal.ONE;
        recipe.setCookingYieldFactor(yieldFactor);

        BigDecimal servings = request.getServingsYield() != null && request.getServingsYield().compareTo(BigDecimal.ZERO) > 0
                ? request.getServingsYield() : BigDecimal.ONE;
        recipe.setServingsYield(servings);

        CookingMethod method = CookingMethod.RAW_ASSEMBLY;
        if (request.getCookingMethod() != null) {
            try {
                method = CookingMethod.valueOf(request.getCookingMethod().toUpperCase());
            } catch (Exception ignored) {}
        }
        recipe.setCookingMethod(method);
        recipe.setUncertaintyRating(UncertaintyRating.MODERATE_HOME_VARIANCE);

        BigDecimal totalRawWeight = BigDecimal.ZERO;
        List<RecipeIngredient> ingredients = new ArrayList<>();

        for (CustomRecipeRequestDto.CustomIngredientItem item : request.getIngredients()) {
            if (item.getFoodId() == null || item.getRawQuantityG() == null || item.getRawQuantityG().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            FoodItem food = foodItemRepository.findByIdWithDetails(item.getFoodId()).orElse(null);
            if (food == null) continue;

            RecipeIngredient ing = new RecipeIngredient();
            ing.setRecipe(recipe);
            ing.setFood(food);
            ing.setRawQuantityG(item.getRawQuantityG());
            ing.setRetentionFactorOverride(item.getRetentionFactorOverride() != null ? item.getRetentionFactorOverride() : BigDecimal.ONE);
            ingredients.add(ing);

            totalRawWeight = totalRawWeight.add(item.getRawQuantityG());
        }

        if (ingredients.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        recipe.setTotalRawWeightG(totalRawWeight);
        BigDecimal cookedWeight = totalRawWeight.multiply(yieldFactor).setScale(2, RoundingMode.HALF_UP);
        recipe.setCookedWeightG(cookedWeight);
        recipe.setServingWeightG(cookedWeight.divide(servings, 2, RoundingMode.HALF_UP));
        recipe.setIngredients(ingredients);

        RecipeCalculationResultDto result = recipeCalculationService.calculateRecipe(recipe);
        return ResponseEntity.ok(result);
    }

    private RecipeDetailDto mapToRecipeDetailDto(Recipe recipe) {
        RecipeDetailDto dto = new RecipeDetailDto();
        dto.setId(recipe.getId());
        dto.setRecipeCode(recipe.getRecipeCode());
        dto.setTitle(recipe.getTitle());
        dto.setDescription(recipe.getDescription());
        dto.setUserId(recipe.getUserId());
        dto.setIsCustom(recipe.getIsCustom() != null ? recipe.getIsCustom() : false);
        dto.setCuisineRegion(recipe.getCuisineRegion());
        dto.setTotalRawWeightG(recipe.getTotalRawWeightG());
        dto.setCookingYieldFactor(recipe.getCookingYieldFactor());
        dto.setCookedWeightG(recipe.getCookedWeightG());
        dto.setServingsYield(recipe.getServingsYield());
        dto.setServingWeightG(recipe.getServingWeightG());
        dto.setCookingMethod(recipe.getCookingMethod() != null ? recipe.getCookingMethod().name() : null);
        dto.setPreparationNotes(recipe.getPreparationNotes());
        dto.setUncertaintyRating(recipe.getUncertaintyRating() != null ? recipe.getUncertaintyRating().name() : null);

        List<RecipeIngredientDto> ingDtos = new ArrayList<>();
        if (recipe.getIngredients() != null) {
            for (RecipeIngredient ing : recipe.getIngredients()) {
                RecipeIngredientDto idto = new RecipeIngredientDto();
                idto.setId(ing.getId());
                if (ing.getFood() != null) {
                    idto.setFoodId(ing.getFood().getId());
                    idto.setFoodCode(ing.getFood().getFoodCode());
                    idto.setFoodName(ing.getFood().getName());
                    idto.setFoodGroup(ing.getFood().getFoodGroup());

                    FoodNutrient nut = ing.getFood().getNutrients().stream()
                            .filter(n -> n.getBasis() == NutrientBasis.PER_100G)
                            .findFirst()
                            .orElse(null);

                    if (nut != null && ing.getRawQuantityG() != null) {
                        BigDecimal factor = ing.getRawQuantityG().divide(RecipeCalculationService.HUNDRED, 4, RoundingMode.HALF_UP);
                        if (nut.getEnergyKcal() != null) idto.setIngredientCalories(nut.getEnergyKcal().multiply(factor).setScale(1, RoundingMode.HALF_UP));
                        if (nut.getProteinG() != null) idto.setIngredientProtein(nut.getProteinG().multiply(factor).setScale(1, RoundingMode.HALF_UP));
                        if (nut.getAvailableCarbohydratesG() != null) idto.setIngredientCarbs(nut.getAvailableCarbohydratesG().multiply(factor).setScale(1, RoundingMode.HALF_UP));
                        if (nut.getTotalFatG() != null) idto.setIngredientFat(nut.getTotalFatG().multiply(factor).setScale(1, RoundingMode.HALF_UP));
                    }
                }
                idto.setRawQuantityG(ing.getRawQuantityG());
                idto.setRetentionFactorOverride(ing.getRetentionFactorOverride());
                ingDtos.add(idto);
            }
        }
        dto.setIngredients(ingDtos);

        try {
            RecipeCalculationResultDto calc = recipeCalculationService.calculateRecipe(recipe);
            dto.setCalculationResult(calc);
        } catch (Exception ignored) {}

        return dto;
    }
}
