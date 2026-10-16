package com.fitkit.dto;

import java.math.BigDecimal;

public class RecipeIngredientDto {
    private Long id;
    private Long foodId;
    private String foodCode;
    private String foodName;
    private String foodGroup;
    private BigDecimal rawQuantityG;
    private BigDecimal retentionFactorOverride;
    private BigDecimal ingredientCalories;
    private BigDecimal ingredientProtein;
    private BigDecimal ingredientCarbs;
    private BigDecimal ingredientFat;

    public RecipeIngredientDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getFoodId() { return foodId; }
    public void setFoodId(Long foodId) { this.foodId = foodId; }

    public String getFoodCode() { return foodCode; }
    public void setFoodCode(String foodCode) { this.foodCode = foodCode; }

    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }

    public String getFoodGroup() { return foodGroup; }
    public void setFoodGroup(String foodGroup) { this.foodGroup = foodGroup; }

    public BigDecimal getRawQuantityG() { return rawQuantityG; }
    public void setRawQuantityG(BigDecimal rawQuantityG) { this.rawQuantityG = rawQuantityG; }

    public BigDecimal getRetentionFactorOverride() { return retentionFactorOverride; }
    public void setRetentionFactorOverride(BigDecimal retentionFactorOverride) { this.retentionFactorOverride = retentionFactorOverride; }

    public BigDecimal getIngredientCalories() { return ingredientCalories; }
    public void setIngredientCalories(BigDecimal ingredientCalories) { this.ingredientCalories = ingredientCalories; }

    public BigDecimal getIngredientProtein() { return ingredientProtein; }
    public void setIngredientProtein(BigDecimal ingredientProtein) { this.ingredientProtein = ingredientProtein; }

    public BigDecimal getIngredientCarbs() { return ingredientCarbs; }
    public void setIngredientCarbs(BigDecimal ingredientCarbs) { this.ingredientCarbs = ingredientCarbs; }

    public BigDecimal getIngredientFat() { return ingredientFat; }
    public void setIngredientFat(BigDecimal ingredientFat) { this.ingredientFat = ingredientFat; }
}
