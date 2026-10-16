package com.fitkit.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class RecipeDetailDto {
    private Long id;
    private String recipeCode;
    private String title;
    private String description;
    private String userId;
    private Boolean isCustom = false;
    private String cuisineRegion;
    private BigDecimal totalRawWeightG;
    private BigDecimal cookingYieldFactor;
    private BigDecimal cookedWeightG;
    private BigDecimal servingsYield;
    private BigDecimal servingWeightG;
    private String cookingMethod;
    private String preparationNotes;
    private String uncertaintyRating;
    private List<RecipeIngredientDto> ingredients = new ArrayList<>();
    private RecipeCalculationResultDto calculationResult;

    public RecipeDetailDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRecipeCode() { return recipeCode; }
    public void setRecipeCode(String recipeCode) { this.recipeCode = recipeCode; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCuisineRegion() { return cuisineRegion; }
    public void setCuisineRegion(String cuisineRegion) { this.cuisineRegion = cuisineRegion; }

    public BigDecimal getTotalRawWeightG() { return totalRawWeightG; }
    public void setTotalRawWeightG(BigDecimal totalRawWeightG) { this.totalRawWeightG = totalRawWeightG; }

    public BigDecimal getCookingYieldFactor() { return cookingYieldFactor; }
    public void setCookingYieldFactor(BigDecimal cookingYieldFactor) { this.cookingYieldFactor = cookingYieldFactor; }

    public BigDecimal getCookedWeightG() { return cookedWeightG; }
    public void setCookedWeightG(BigDecimal cookedWeightG) { this.cookedWeightG = cookedWeightG; }

    public BigDecimal getServingsYield() { return servingsYield; }
    public void setServingsYield(BigDecimal servingsYield) { this.servingsYield = servingsYield; }

    public BigDecimal getServingWeightG() { return servingWeightG; }
    public void setServingWeightG(BigDecimal servingWeightG) { this.servingWeightG = servingWeightG; }

    public String getCookingMethod() { return cookingMethod; }
    public void setCookingMethod(String cookingMethod) { this.cookingMethod = cookingMethod; }

    public String getPreparationNotes() { return preparationNotes; }
    public void setPreparationNotes(String preparationNotes) { this.preparationNotes = preparationNotes; }

    public String getUncertaintyRating() { return uncertaintyRating; }
    public void setUncertaintyRating(String uncertaintyRating) { this.uncertaintyRating = uncertaintyRating; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Boolean getIsCustom() { return isCustom; }
    public void setIsCustom(Boolean isCustom) { this.isCustom = isCustom; }

    public List<RecipeIngredientDto> getIngredients() { return ingredients; }
    public void setIngredients(List<RecipeIngredientDto> ingredients) { this.ingredients = ingredients; }

    public RecipeCalculationResultDto getCalculationResult() { return calculationResult; }
    public void setCalculationResult(RecipeCalculationResultDto calculationResult) { this.calculationResult = calculationResult; }
}
