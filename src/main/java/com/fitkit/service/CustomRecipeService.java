package com.fitkit.service;

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
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CustomRecipeService {

    private final RecipeRepository recipeRepository;
    private final FoodItemRepository foodItemRepository;
    private final RecipeCalculationService recipeCalculationService;
    private final jakarta.persistence.EntityManager entityManager;

    public CustomRecipeService(RecipeRepository recipeRepository,
                               FoodItemRepository foodItemRepository,
                               RecipeCalculationService recipeCalculationService,
                               jakarta.persistence.EntityManager entityManager) {
        this.recipeRepository = recipeRepository;
        this.foodItemRepository = foodItemRepository;
        this.recipeCalculationService = recipeCalculationService;
        this.entityManager = entityManager;
    }

    @Transactional
    public RecipeDetailDto createCustomRecipe(String userId, CustomRecipeRequestDto request) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new SecurityException("Authentication required to create custom recipes.");
        }
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Recipe title is required.");
        }
        if (request.getIngredients() == null || request.getIngredients().isEmpty()) {
            throw new IllegalArgumentException("Recipe must contain at least one ingredient.");
        }

        Recipe recipe = new Recipe();
        recipe.setRecipeCode("CUSTOM_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        recipe.setUserId(userId.trim().toLowerCase());
        recipe.setIsCustom(true);
        recipe.setIsActive(true);
        recipe.setTitle(request.getTitle().trim());
        recipe.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        recipe.setCuisineRegion(request.getCuisineRegion() != null ? request.getCuisineRegion().trim() : "Custom Home Recipe");

        CookingMethod method = CookingMethod.PRESSURE_COOKING;
        if (request.getCookingMethod() != null) {
            try {
                method = CookingMethod.valueOf(request.getCookingMethod().toUpperCase());
            } catch (Exception ignored) {}
        }
        recipe.setCookingMethod(method);
        recipe.setUncertaintyRating(UncertaintyRating.MODERATE_HOME_VARIANCE);

        BigDecimal servings = (request.getServingsYield() != null && request.getServingsYield().compareTo(BigDecimal.ZERO) > 0)
                ? request.getServingsYield() : BigDecimal.ONE;
        recipe.setServingsYield(servings);

        // Build and validate ingredients
        BigDecimal totalRawWeight = BigDecimal.ZERO;
        List<RecipeIngredient> ingredients = new ArrayList<>();

        for (CustomRecipeRequestDto.CustomIngredientItem item : request.getIngredients()) {
            if (item.getFoodId() == null) {
                throw new IllegalArgumentException("Food ID must be provided for every ingredient.");
            }
            if (item.getRawQuantityG() == null || item.getRawQuantityG().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Quantity must be positive for all ingredients.");
            }

            FoodItem food = foodItemRepository.findByIdWithDetails(item.getFoodId())
                    .orElseThrow(() -> new IllegalArgumentException("Ingredient food item not found with ID: " + item.getFoodId()));

            RecipeIngredient ing = new RecipeIngredient();
            ing.setRecipe(recipe);
            ing.setFood(food);
            ing.setRawQuantityG(item.getRawQuantityG());
            ing.setUnitMeasure(item.getUnitMeasure() != null ? item.getUnitMeasure() : "g");
            ing.setRetentionFactorOverride(item.getRetentionFactorOverride() != null ? item.getRetentionFactorOverride() : BigDecimal.ONE);
            ing.setNotes(item.getNotes());
            ingredients.add(ing);

            totalRawWeight = totalRawWeight.add(item.getRawQuantityG());
        }

        recipe.setTotalRawWeightG(totalRawWeight);
        recipe.setIngredients(ingredients);

        // Authoritative Cooked Weight vs Yield Factor Resolution
        resolveYieldAndWeights(recipe, request.getCookingYieldFactor(), request.getCookedWeightG(), totalRawWeight, servings);

        Recipe saved = recipeRepository.save(recipe);
        return mapToDetailDto(saved);
    }

    @Transactional(readOnly = true)
    public List<RecipeDetailDto> getUserCustomRecipes(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new SecurityException("Authentication required to view custom recipes.");
        }
        String uid = userId.trim().toLowerCase();
        List<Recipe> recipes = recipeRepository.findByUserIdWithIngredients(uid);
        return recipes.stream()
                .map(this::mapToDetailDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RecipeDetailDto getCustomRecipeById(String userId, Long recipeId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new SecurityException("Authentication required.");
        }
        String uid = userId.trim().toLowerCase();

        Recipe recipe = recipeRepository.findByIdWithIngredients(recipeId)
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found with ID: " + recipeId));

        if (!recipe.getIsActive()) {
            throw new EntityNotFoundException("Recipe not found or has been deleted.");
        }

        // Must be canonical or owned by user
        if (recipe.getIsCustom() && (recipe.getUserId() == null || !recipe.getUserId().equalsIgnoreCase(uid))) {
            throw new SecurityException("Access denied: You do not have permission to view this custom recipe.");
        }

        return mapToDetailDto(recipe);
    }

    @Transactional
    public RecipeDetailDto updateCustomRecipe(String userId, Long recipeId, CustomRecipeRequestDto request) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new SecurityException("Authentication required.");
        }
        String uid = userId.trim().toLowerCase();

        Recipe recipe = recipeRepository.findByIdWithIngredients(recipeId)
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found with ID: " + recipeId));

        if (!recipe.getIsActive()) {
            throw new EntityNotFoundException("Recipe not found with ID: " + recipeId);
        }

        // Ownership enforcement
        if (recipe.getUserId() == null || !recipe.getUserId().equalsIgnoreCase(uid)) {
            throw new SecurityException("Access denied: You do not own this recipe.");
        }

        recipe.setTitle(request.getTitle().trim());
        recipe.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        if (request.getCuisineRegion() != null) {
            recipe.setCuisineRegion(request.getCuisineRegion().trim());
        }

        if (request.getCookingMethod() != null) {
            try {
                recipe.setCookingMethod(CookingMethod.valueOf(request.getCookingMethod().toUpperCase()));
            } catch (Exception ignored) {}
        }

        BigDecimal servings = (request.getServingsYield() != null && request.getServingsYield().compareTo(BigDecimal.ZERO) > 0)
                ? request.getServingsYield() : BigDecimal.ONE;
        recipe.setServingsYield(servings);

        // Update ingredients: clear existing and flush orphan deletes to avoid uq_recipe_ingredient constraint conflicts
        recipe.getIngredients().clear();
        entityManager.flush();
        BigDecimal totalRawWeight = BigDecimal.ZERO;

        for (CustomRecipeRequestDto.CustomIngredientItem item : request.getIngredients()) {
            if (item.getFoodId() == null || item.getRawQuantityG() == null || item.getRawQuantityG().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            FoodItem food = foodItemRepository.findByIdWithDetails(item.getFoodId())
                    .orElseThrow(() -> new IllegalArgumentException("Ingredient food item not found with ID: " + item.getFoodId()));

            RecipeIngredient ing = new RecipeIngredient();
            ing.setRecipe(recipe);
            ing.setFood(food);
            ing.setRawQuantityG(item.getRawQuantityG());
            ing.setUnitMeasure(item.getUnitMeasure() != null ? item.getUnitMeasure() : "g");
            ing.setRetentionFactorOverride(item.getRetentionFactorOverride() != null ? item.getRetentionFactorOverride() : BigDecimal.ONE);
            ing.setNotes(item.getNotes());
            recipe.getIngredients().add(ing);

            totalRawWeight = totalRawWeight.add(item.getRawQuantityG());
        }

        if (recipe.getIngredients().isEmpty()) {
            throw new IllegalArgumentException("Recipe must contain at least one valid ingredient.");
        }

        recipe.setTotalRawWeightG(totalRawWeight);
        resolveYieldAndWeights(recipe, request.getCookingYieldFactor(), request.getCookedWeightG(), totalRawWeight, servings);

        Recipe saved = recipeRepository.save(recipe);
        return mapToDetailDto(saved);
    }

    @Transactional
    public void deleteCustomRecipe(String userId, Long recipeId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new SecurityException("Authentication required.");
        }
        String uid = userId.trim().toLowerCase();

        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException("Recipe not found with ID: " + recipeId));

        if (recipe.getUserId() == null || !recipe.getUserId().equalsIgnoreCase(uid)) {
            throw new SecurityException("Access denied: You do not own this recipe.");
        }

        // Soft deletion preserves historical references in meal_entries
        recipe.setIsActive(false);
        recipeRepository.save(recipe);
    }

    @Transactional(readOnly = true)
    public RecipeCalculationResultDto calculatePreview(CustomRecipeRequestDto request) {
        if (request.getIngredients() == null || request.getIngredients().isEmpty()) {
            throw new IllegalArgumentException("At least one ingredient is required for recipe preview.");
        }

        Recipe recipe = new Recipe();
        recipe.setRecipeCode("PREVIEW_" + System.currentTimeMillis());
        recipe.setTitle(request.getTitle() != null && !request.getTitle().trim().isEmpty() ? request.getTitle().trim() : "Custom Recipe Preview");

        BigDecimal servings = (request.getServingsYield() != null && request.getServingsYield().compareTo(BigDecimal.ZERO) > 0)
                ? request.getServingsYield() : BigDecimal.ONE;
        recipe.setServingsYield(servings);

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
            throw new IllegalArgumentException("No valid ingredients found for calculation.");
        }

        recipe.setTotalRawWeightG(totalRawWeight);
        recipe.setIngredients(ingredients);
        resolveYieldAndWeights(recipe, request.getCookingYieldFactor(), request.getCookedWeightG(), totalRawWeight, servings);

        return recipeCalculationService.calculateRecipe(recipe);
    }

    private void resolveYieldAndWeights(Recipe recipe, BigDecimal reqYieldFactor, BigDecimal reqCookedWeight, BigDecimal totalRawWeight, BigDecimal servings) {
        BigDecimal cookedWeight;
        BigDecimal yieldFactor;

        if (reqCookedWeight != null && reqCookedWeight.compareTo(BigDecimal.ZERO) > 0) {
            cookedWeight = reqCookedWeight.setScale(2, RoundingMode.HALF_UP);
            yieldFactor = cookedWeight.divide(totalRawWeight, 4, RoundingMode.HALF_UP);
        } else if (reqYieldFactor != null && reqYieldFactor.compareTo(BigDecimal.ZERO) > 0) {
            yieldFactor = reqYieldFactor;
            cookedWeight = totalRawWeight.multiply(yieldFactor).setScale(2, RoundingMode.HALF_UP);
        } else {
            yieldFactor = BigDecimal.ONE.setScale(3, RoundingMode.HALF_UP);
            cookedWeight = totalRawWeight.setScale(2, RoundingMode.HALF_UP);
        }

        recipe.setCookingYieldFactor(yieldFactor);
        recipe.setCookedWeightG(cookedWeight);
        recipe.setServingWeightG(cookedWeight.divide(servings, 2, RoundingMode.HALF_UP));
    }

    public RecipeDetailDto mapToDetailDto(Recipe recipe) {
        RecipeDetailDto dto = new RecipeDetailDto();
        dto.setId(recipe.getId());
        dto.setRecipeCode(recipe.getRecipeCode());
        dto.setTitle(recipe.getTitle());
        dto.setDescription(recipe.getDescription());
        dto.setUserId(recipe.getUserId());
        dto.setIsCustom(recipe.getIsCustom());
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

                    FoodNutrient nut = ing.getFood().getNutrients() != null ? ing.getFood().getNutrients().stream()
                            .filter(n -> n.getBasis() == NutrientBasis.PER_100G)
                            .findFirst()
                            .orElse(null) : null;

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
