package com.fitkit.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CreateMealEntryRequestDto {
    private String mealType; // Breakfast, Lunch, Dinner, Snacks
    private Long foodId;
    private Long recipeId;
    private String portionDescription;
    private BigDecimal portionGramWeight;
    private BigDecimal quantityMultiplier = BigDecimal.ONE;
    private LocalDate logDate;

    public CreateMealEntryRequestDto() {}

    public String getMealType() { return mealType; }
    public void setMealType(String mealType) { this.mealType = mealType; }

    public Long getFoodId() { return foodId; }
    public void setFoodId(Long foodId) { this.foodId = foodId; }

    public Long getRecipeId() { return recipeId; }
    public void setRecipeId(Long recipeId) { this.recipeId = recipeId; }

    public String getPortionDescription() { return portionDescription; }
    public void setPortionDescription(String portionDescription) { this.portionDescription = portionDescription; }

    public BigDecimal getPortionGramWeight() { return portionGramWeight; }
    public void setPortionGramWeight(BigDecimal portionGramWeight) { this.portionGramWeight = portionGramWeight; }

    public BigDecimal getQuantityMultiplier() { return quantityMultiplier; }
    public void setQuantityMultiplier(BigDecimal quantityMultiplier) { this.quantityMultiplier = quantityMultiplier; }

    public LocalDate getLogDate() { return logDate; }
    public void setLogDate(LocalDate logDate) { this.logDate = logDate; }
}
