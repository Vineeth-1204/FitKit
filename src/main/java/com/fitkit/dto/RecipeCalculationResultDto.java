package com.fitkit.dto;

import java.math.BigDecimal;
import java.util.List;

public class RecipeCalculationResultDto {
    private String recipeCode;
    private String title;
    private BigDecimal totalRawWeightG;
    private BigDecimal cookingYieldFactor;
    private BigDecimal cookedWeightG;
    private BigDecimal servingsCount;
    private BigDecimal servingWeightG;

    // Per 100g Cooked
    private BigDecimal energyKcalPer100g;
    private BigDecimal energyKjPer100g;
    private BigDecimal proteinGPer100g;
    private BigDecimal availableCarbsGPer100g;
    private BigDecimal fatGPer100g;
    private BigDecimal fiberGPer100g;

    // Per Serving
    private BigDecimal energyKcalPerServing;
    private BigDecimal proteinGPerServing;
    private BigDecimal availableCarbsGPerServing;
    private BigDecimal fatGPerServing;
    private BigDecimal fiberGPerServing;

    // Full Batch Totals
    private BigDecimal totalBatchCalories;
    private BigDecimal totalBatchProtein;
    private BigDecimal totalBatchCarbs;
    private BigDecimal totalBatchFat;
    private BigDecimal totalBatchFiber;

    private Boolean hasIncompleteNutrients = false;
    private List<String> calculationNotes;
    private String uncertaintyRating;

    public RecipeCalculationResultDto() {}

    public String getRecipeCode() { return recipeCode; }
    public void setRecipeCode(String recipeCode) { this.recipeCode = recipeCode; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public BigDecimal getTotalRawWeightG() { return totalRawWeightG; }
    public void setTotalRawWeightG(BigDecimal totalRawWeightG) { this.totalRawWeightG = totalRawWeightG; }

    public BigDecimal getCookingYieldFactor() { return cookingYieldFactor; }
    public void setCookingYieldFactor(BigDecimal cookingYieldFactor) { this.cookingYieldFactor = cookingYieldFactor; }

    public BigDecimal getCookedWeightG() { return cookedWeightG; }
    public void setCookedWeightG(BigDecimal cookedWeightG) { this.cookedWeightG = cookedWeightG; }

    public BigDecimal getServingsCount() { return servingsCount; }
    public void setServingsCount(BigDecimal servingsCount) { this.servingsCount = servingsCount; }

    public BigDecimal getServingWeightG() { return servingWeightG; }
    public void setServingWeightG(BigDecimal servingWeightG) { this.servingWeightG = servingWeightG; }

    public BigDecimal getEnergyKcalPer100g() { return energyKcalPer100g; }
    public void setEnergyKcalPer100g(BigDecimal energyKcalPer100g) { this.energyKcalPer100g = energyKcalPer100g; }

    public BigDecimal getEnergyKjPer100g() { return energyKjPer100g; }
    public void setEnergyKjPer100g(BigDecimal energyKjPer100g) { this.energyKjPer100g = energyKjPer100g; }

    public BigDecimal getProteinGPer100g() { return proteinGPer100g; }
    public void setProteinGPer100g(BigDecimal proteinGPer100g) { this.proteinGPer100g = proteinGPer100g; }

    public BigDecimal getAvailableCarbsGPer100g() { return availableCarbsGPer100g; }
    public void setAvailableCarbsGPer100g(BigDecimal availableCarbsGPer100g) { this.availableCarbsGPer100g = availableCarbsGPer100g; }

    public BigDecimal getFatGPer100g() { return fatGPer100g; }
    public void setFatGPer100g(BigDecimal fatGPer100g) { this.fatGPer100g = fatGPer100g; }

    public BigDecimal getFiberGPer100g() { return fiberGPer100g; }
    public void setFiberGPer100g(BigDecimal fiberGPer100g) { this.fiberGPer100g = fiberGPer100g; }

    public BigDecimal getEnergyKcalPerServing() { return energyKcalPerServing; }
    public void setEnergyKcalPerServing(BigDecimal energyKcalPerServing) { this.energyKcalPerServing = energyKcalPerServing; }

    public BigDecimal getProteinGPerServing() { return proteinGPerServing; }
    public void setProteinGPerServing(BigDecimal proteinGPerServing) { this.proteinGPerServing = proteinGPerServing; }

    public BigDecimal getAvailableCarbsGPerServing() { return availableCarbsGPerServing; }
    public void setAvailableCarbsGPerServing(BigDecimal availableCarbsGPerServing) { this.availableCarbsGPerServing = availableCarbsGPerServing; }

    public BigDecimal getFatGPerServing() { return fatGPerServing; }
    public void setFatGPerServing(BigDecimal fatGPerServing) { this.fatGPerServing = fatGPerServing; }

    public BigDecimal getFiberGPerServing() { return fiberGPerServing; }
    public void setFiberGPerServing(BigDecimal fiberGPerServing) { this.fiberGPerServing = fiberGPerServing; }

    public BigDecimal getTotalBatchCalories() { return totalBatchCalories; }
    public void setTotalBatchCalories(BigDecimal totalBatchCalories) { this.totalBatchCalories = totalBatchCalories; }

    public BigDecimal getTotalBatchProtein() { return totalBatchProtein; }
    public void setTotalBatchProtein(BigDecimal totalBatchProtein) { this.totalBatchProtein = totalBatchProtein; }

    public BigDecimal getTotalBatchCarbs() { return totalBatchCarbs; }
    public void setTotalBatchCarbs(BigDecimal totalBatchCarbs) { this.totalBatchCarbs = totalBatchCarbs; }

    public BigDecimal getTotalBatchFat() { return totalBatchFat; }
    public void setTotalBatchFat(BigDecimal totalBatchFat) { this.totalBatchFat = totalBatchFat; }

    public BigDecimal getTotalBatchFiber() { return totalBatchFiber; }
    public void setTotalBatchFiber(BigDecimal totalBatchFiber) { this.totalBatchFiber = totalBatchFiber; }

    public Boolean getHasIncompleteNutrients() { return hasIncompleteNutrients; }
    public void setHasIncompleteNutrients(Boolean hasIncompleteNutrients) { this.hasIncompleteNutrients = hasIncompleteNutrients; }

    public List<String> getCalculationNotes() { return calculationNotes; }
    public void setCalculationNotes(List<String> calculationNotes) { this.calculationNotes = calculationNotes; }

    public String getUncertaintyRating() { return uncertaintyRating; }
    public void setUncertaintyRating(String uncertaintyRating) { this.uncertaintyRating = uncertaintyRating; }
}
