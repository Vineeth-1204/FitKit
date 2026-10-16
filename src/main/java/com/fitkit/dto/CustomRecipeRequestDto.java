package com.fitkit.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CustomRecipeRequestDto {

    @NotBlank(message = "Recipe title is required.")
    private String title;

    private String description;

    private String cuisineRegion;

    @Positive(message = "Cooking yield factor must be positive.")
    private BigDecimal cookingYieldFactor;

    @Positive(message = "Cooked weight must be positive.")
    private BigDecimal cookedWeightG;

    @NotNull(message = "Servings yield is required.")
    @Positive(message = "Servings yield must be positive.")
    private BigDecimal servingsYield = BigDecimal.ONE;

    private String cookingMethod = "PRESSURE_COOKING";

    @NotEmpty(message = "Recipe must contain at least one ingredient.")
    @Valid
    private List<CustomIngredientItem> ingredients = new ArrayList<>();

    public static class CustomIngredientItem {
        @NotNull(message = "Food ID is required.")
        private Long foodId;

        @NotNull(message = "Ingredient quantity is required.")
        @Positive(message = "Ingredient quantity must be positive.")
        private BigDecimal rawQuantityG;

        private String unitMeasure = "g";

        private BigDecimal retentionFactorOverride;

        private String notes;

        public CustomIngredientItem() {}

        public CustomIngredientItem(Long foodId, BigDecimal rawQuantityG, String unitMeasure) {
            this.foodId = foodId;
            this.rawQuantityG = rawQuantityG;
            this.unitMeasure = unitMeasure != null ? unitMeasure : "g";
        }

        public Long getFoodId() { return foodId; }
        public void setFoodId(Long foodId) { this.foodId = foodId; }

        public BigDecimal getRawQuantityG() { return rawQuantityG; }
        public void setRawQuantityG(BigDecimal rawQuantityG) { this.rawQuantityG = rawQuantityG; }

        public String getUnitMeasure() { return unitMeasure; }
        public void setUnitMeasure(String unitMeasure) { this.unitMeasure = unitMeasure; }

        public BigDecimal getRetentionFactorOverride() { return retentionFactorOverride; }
        public void setRetentionFactorOverride(BigDecimal retentionFactorOverride) { this.retentionFactorOverride = retentionFactorOverride; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    public CustomRecipeRequestDto() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCuisineRegion() { return cuisineRegion; }
    public void setCuisineRegion(String cuisineRegion) { this.cuisineRegion = cuisineRegion; }

    public BigDecimal getCookingYieldFactor() { return cookingYieldFactor; }
    public void setCookingYieldFactor(BigDecimal cookingYieldFactor) { this.cookingYieldFactor = cookingYieldFactor; }

    public BigDecimal getCookedWeightG() { return cookedWeightG; }
    public void setCookedWeightG(BigDecimal cookedWeightG) { this.cookedWeightG = cookedWeightG; }

    public BigDecimal getServingsYield() { return servingsYield; }
    public void setServingsYield(BigDecimal servingsYield) { this.servingsYield = servingsYield; }

    public String getCookingMethod() { return cookingMethod; }
    public void setCookingMethod(String cookingMethod) { this.cookingMethod = cookingMethod; }

    public List<CustomIngredientItem> getIngredients() { return ingredients; }
    public void setIngredients(List<CustomIngredientItem> ingredients) { this.ingredients = ingredients; }
}
