package com.fitkit.service;

import com.fitkit.domain.entity.FoodNutrient;
import com.fitkit.domain.entity.Recipe;
import com.fitkit.domain.entity.RecipeIngredient;
import com.fitkit.domain.enums.NutrientBasis;
import com.fitkit.dto.RecipeCalculationResultDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class RecipeCalculationService {

    public static final BigDecimal HUNDRED = new BigDecimal("100.0");

    /**
     * Calculates the nutritional profile of a recipe accounting for:
     * - Individual raw ingredient weights and per-100g nutrient profiles
     * - Cooking yield factor (water absorption / evaporation)
     * - Cooked batch weight and serving divisions
     * - Nutrient retention factors
     * - Documented estimation uncertainty
     */
    public RecipeCalculationResultDto calculateRecipe(Recipe recipe) {
        if (recipe == null) {
            throw new IllegalArgumentException("Recipe cannot be null");
        }
        if (recipe.getCookingYieldFactor() == null || recipe.getCookingYieldFactor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Cooking yield factor must be positive and non-zero");
        }
        if (recipe.getServingsYield() == null || recipe.getServingsYield().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Servings yield must be positive and non-zero");
        }
        if (recipe.getIngredients() == null || recipe.getIngredients().isEmpty()) {
            throw new IllegalArgumentException("Recipe must contain at least one ingredient");
        }

        BigDecimal totalRawWeight = BigDecimal.ZERO;
        BigDecimal totalBatchCalories = BigDecimal.ZERO;
        BigDecimal totalBatchProtein = BigDecimal.ZERO;
        BigDecimal totalBatchCarbs = BigDecimal.ZERO;
        BigDecimal totalBatchFat = BigDecimal.ZERO;
        BigDecimal totalBatchFiber = BigDecimal.ZERO;

        boolean hasIncompleteNutrients = false;
        List<String> calculationNotes = new ArrayList<>();

        for (RecipeIngredient ing : recipe.getIngredients()) {
            if (ing.getRawQuantityG() == null || ing.getRawQuantityG().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Ingredient quantity must be positive for ingredient: " +
                        (ing.getFood() != null ? ing.getFood().getName() : "Unknown"));
            }

            BigDecimal qtyG = ing.getRawQuantityG();
            totalRawWeight = totalRawWeight.add(qtyG);

            // Find per-100g nutrients of the ingredient
            FoodNutrient nut = (ing.getFood() != null && ing.getFood().getNutrients() != null)
                    ? ing.getFood().getNutrients().stream()
                    .filter(n -> n.getBasis() == NutrientBasis.PER_100G)
                    .findFirst()
                    .orElse(null)
                    : null;

            if (nut == null) {
                hasIncompleteNutrients = true;
                calculationNotes.add("WARNING: Ingredient [" + (ing.getFood() != null ? ing.getFood().getName() : "Unknown") +
                        "] lacks per-100g nutrient baseline. Excluded from aggregation.");
                continue;
            }

            BigDecimal factor = qtyG.divide(HUNDRED, 4, RoundingMode.HALF_UP);
            BigDecimal retention = ing.getRetentionFactorOverride() != null ? ing.getRetentionFactorOverride() : BigDecimal.ONE;

            if (nut.getEnergyKcal() != null) {
                totalBatchCalories = totalBatchCalories.add(nut.getEnergyKcal().multiply(factor));
            } else {
                hasIncompleteNutrients = true;
                calculationNotes.add("NOTICE: Ingredient [" + ing.getFood().getName() + "] has unrecorded calorie value.");
            }

            if (nut.getProteinG() != null) {
                totalBatchProtein = totalBatchProtein.add(nut.getProteinG().multiply(factor).multiply(retention));
            } else {
                hasIncompleteNutrients = true;
            }

            if (nut.getAvailableCarbohydratesG() != null) {
                totalBatchCarbs = totalBatchCarbs.add(nut.getAvailableCarbohydratesG().multiply(factor));
            } else {
                hasIncompleteNutrients = true;
            }

            if (nut.getTotalFatG() != null) {
                totalBatchFat = totalBatchFat.add(nut.getTotalFatG().multiply(factor));
            } else {
                hasIncompleteNutrients = true;
            }

            if (nut.getDietaryFiberG() != null) {
                totalBatchFiber = totalBatchFiber.add(nut.getDietaryFiberG().multiply(factor));
            } else {
                hasIncompleteNutrients = true;
            }
        }

        if (totalRawWeight.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Total raw weight of recipe ingredients must be greater than zero.");
        }

        // Authoritative Cooked Weight vs Yield Factor Resolution:
        // Do not apply cooking yield twice. If user specifies final cooked batch weight, use that as the authoritative input.
        BigDecimal calculatedCookedWeight;
        BigDecimal yieldFactor;

        if (recipe.getCookedWeightG() != null && recipe.getCookedWeightG().compareTo(BigDecimal.ZERO) > 0) {
            calculatedCookedWeight = recipe.getCookedWeightG().setScale(2, RoundingMode.HALF_UP);
            yieldFactor = calculatedCookedWeight.divide(totalRawWeight, 4, RoundingMode.HALF_UP);
        } else if (recipe.getCookingYieldFactor() != null && recipe.getCookingYieldFactor().compareTo(BigDecimal.ZERO) > 0) {
            yieldFactor = recipe.getCookingYieldFactor();
            calculatedCookedWeight = totalRawWeight.multiply(yieldFactor).setScale(2, RoundingMode.HALF_UP);
        } else {
            // Default 1.0x (raw assembly / no net moisture change)
            yieldFactor = BigDecimal.ONE.setScale(3, RoundingMode.HALF_UP);
            calculatedCookedWeight = totalRawWeight.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal servings = (recipe.getServingsYield() != null && recipe.getServingsYield().compareTo(BigDecimal.ZERO) > 0)
                ? recipe.getServingsYield()
                : BigDecimal.ONE;

        BigDecimal servingWeight = calculatedCookedWeight.divide(servings, 2, RoundingMode.HALF_UP);

        // Calculate nutrient density per 100g of final cooked food
        BigDecimal energyPer100g = totalBatchCalories.multiply(HUNDRED).divide(calculatedCookedWeight, 2, RoundingMode.HALF_UP);
        BigDecimal proteinPer100g = totalBatchProtein.multiply(HUNDRED).divide(calculatedCookedWeight, 2, RoundingMode.HALF_UP);
        BigDecimal carbsPer100g = totalBatchCarbs.multiply(HUNDRED).divide(calculatedCookedWeight, 2, RoundingMode.HALF_UP);
        BigDecimal fatPer100g = totalBatchFat.multiply(HUNDRED).divide(calculatedCookedWeight, 2, RoundingMode.HALF_UP);
        BigDecimal fiberPer100g = totalBatchFiber.multiply(HUNDRED).divide(calculatedCookedWeight, 2, RoundingMode.HALF_UP);
        BigDecimal energyKjPer100g = energyPer100g.multiply(NormalizationService.KJ_TO_KCAL_FACTOR).setScale(2, RoundingMode.HALF_UP);

        // Calculate nutrient quantity per serving
        BigDecimal energyPerServing = totalBatchCalories.divide(servings, 2, RoundingMode.HALF_UP);
        BigDecimal proteinPerServing = totalBatchProtein.divide(servings, 2, RoundingMode.HALF_UP);
        BigDecimal carbsPerServing = totalBatchCarbs.divide(servings, 2, RoundingMode.HALF_UP);
        BigDecimal fatPerServing = totalBatchFat.divide(servings, 2, RoundingMode.HALF_UP);
        BigDecimal fiberPerServing = totalBatchFiber.divide(servings, 2, RoundingMode.HALF_UP);

        calculationNotes.add("Raw batch weight: " + totalRawWeight.setScale(1, RoundingMode.HALF_UP) + "g | Yield multiplier: " +
                yieldFactor.setScale(2, RoundingMode.HALF_UP) + "x | Final cooked batch: " +
                calculatedCookedWeight.setScale(1, RoundingMode.HALF_UP) + "g (" + servings + " servings of " + servingWeight + "g each).");

        RecipeCalculationResultDto result = new RecipeCalculationResultDto();
        result.setRecipeCode(recipe.getRecipeCode());
        result.setTitle(recipe.getTitle());
        result.setTotalRawWeightG(totalRawWeight.setScale(1, RoundingMode.HALF_UP));
        result.setCookingYieldFactor(yieldFactor.setScale(3, RoundingMode.HALF_UP));
        result.setCookedWeightG(calculatedCookedWeight);
        result.setServingsCount(servings);
        result.setServingWeightG(servingWeight);

        // Batch totals
        result.setTotalBatchCalories(totalBatchCalories.setScale(1, RoundingMode.HALF_UP));
        result.setTotalBatchProtein(totalBatchProtein.setScale(1, RoundingMode.HALF_UP));
        result.setTotalBatchCarbs(totalBatchCarbs.setScale(1, RoundingMode.HALF_UP));
        result.setTotalBatchFat(totalBatchFat.setScale(1, RoundingMode.HALF_UP));
        result.setTotalBatchFiber(totalBatchFiber.setScale(1, RoundingMode.HALF_UP));

        // Density per 100g
        result.setEnergyKcalPer100g(energyPer100g);
        result.setEnergyKjPer100g(energyKjPer100g);
        result.setProteinGPer100g(proteinPer100g);
        result.setAvailableCarbsGPer100g(carbsPer100g);
        result.setFatGPer100g(fatPer100g);
        result.setFiberGPer100g(fiberPer100g);

        // Per serving
        result.setEnergyKcalPerServing(energyPerServing);
        result.setProteinGPerServing(proteinPerServing);
        result.setAvailableCarbsGPerServing(carbsPerServing);
        result.setFatGPerServing(fatPerServing);
        result.setFiberGPerServing(fiberPerServing);

        result.setHasIncompleteNutrients(hasIncompleteNutrients);
        result.setCalculationNotes(calculationNotes);
        result.setUncertaintyRating(recipe.getUncertaintyRating() != null ? recipe.getUncertaintyRating().name() : "MODERATE_HOME_VARIANCE");

        return result;
    }
}
