package com.fitkit.dto;

import java.math.BigDecimal;

public class MealEntryDto {
    private Long id;
    private String mealType;
    private Long foodId;
    private Long recipeId;
    private String foodName;
    private String foodState;
    private String portionDescription;
    private BigDecimal portionGramWeight;
    private BigDecimal quantityMultiplier;
    private BigDecimal totalGramWeight;
    private BigDecimal energyKcal;
    private BigDecimal energyKj;
    private BigDecimal proteinG;
    private BigDecimal carbohydratesG;
    private BigDecimal fatG;
    private BigDecimal fiberG;

    public MealEntryDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMealType() { return mealType; }
    public void setMealType(String mealType) { this.mealType = mealType; }

    public Long getFoodId() { return foodId; }
    public void setFoodId(Long foodId) { this.foodId = foodId; }

    public Long getRecipeId() { return recipeId; }
    public void setRecipeId(Long recipeId) { this.recipeId = recipeId; }

    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }

    public String getFoodState() { return foodState; }
    public void setFoodState(String foodState) { this.foodState = foodState; }

    public String getPortionDescription() { return portionDescription; }
    public void setPortionDescription(String portionDescription) { this.portionDescription = portionDescription; }

    public BigDecimal getPortionGramWeight() { return portionGramWeight; }
    public void setPortionGramWeight(BigDecimal portionGramWeight) { this.portionGramWeight = portionGramWeight; }

    public BigDecimal getQuantityMultiplier() { return quantityMultiplier; }
    public void setQuantityMultiplier(BigDecimal quantityMultiplier) { this.quantityMultiplier = quantityMultiplier; }

    public BigDecimal getTotalGramWeight() { return totalGramWeight; }
    public void setTotalGramWeight(BigDecimal totalGramWeight) { this.totalGramWeight = totalGramWeight; }

    public BigDecimal getEnergyKcal() { return energyKcal; }
    public void setEnergyKcal(BigDecimal energyKcal) { this.energyKcal = energyKcal; }

    public BigDecimal getEnergyKj() { return energyKj; }
    public void setEnergyKj(BigDecimal energyKj) { this.energyKj = energyKj; }

    public BigDecimal getProteinG() { return proteinG; }
    public void setProteinG(BigDecimal proteinG) { this.proteinG = proteinG; }

    public BigDecimal getCarbohydratesG() { return carbohydratesG; }
    public void setCarbohydratesG(BigDecimal carbohydratesG) { this.carbohydratesG = carbohydratesG; }

    public BigDecimal getFatG() { return fatG; }
    public void setFatG(BigDecimal fatG) { this.fatG = fatG; }

    public BigDecimal getFiberG() { return fiberG; }
    public void setFiberG(BigDecimal fiberG) { this.fiberG = fiberG; }
}
