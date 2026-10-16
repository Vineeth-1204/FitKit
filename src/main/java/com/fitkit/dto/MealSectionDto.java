package com.fitkit.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MealSectionDto {
    private String mealType;
    private List<MealEntryDto> entries = new ArrayList<>();
    private BigDecimal subtotalCalories = BigDecimal.ZERO;
    private BigDecimal subtotalProteinG = BigDecimal.ZERO;
    private BigDecimal subtotalCarbsG = BigDecimal.ZERO;
    private BigDecimal subtotalFatG = BigDecimal.ZERO;
    private BigDecimal subtotalFiberG = BigDecimal.ZERO;

    public MealSectionDto() {}

    public MealSectionDto(String mealType) {
        this.mealType = mealType;
    }

    public String getMealType() { return mealType; }
    public void setMealType(String mealType) { this.mealType = mealType; }

    public List<MealEntryDto> getEntries() { return entries; }
    public void setEntries(List<MealEntryDto> entries) { this.entries = entries; }

    public BigDecimal getSubtotalCalories() { return subtotalCalories; }
    public void setSubtotalCalories(BigDecimal subtotalCalories) { this.subtotalCalories = subtotalCalories; }

    public BigDecimal getSubtotalProteinG() { return subtotalProteinG; }
    public void setSubtotalProteinG(BigDecimal subtotalProteinG) { this.subtotalProteinG = subtotalProteinG; }

    public BigDecimal getSubtotalCarbsG() { return subtotalCarbsG; }
    public void setSubtotalCarbsG(BigDecimal subtotalCarbsG) { this.subtotalCarbsG = subtotalCarbsG; }

    public BigDecimal getSubtotalFatG() { return subtotalFatG; }
    public void setSubtotalFatG(BigDecimal subtotalFatG) { this.subtotalFatG = subtotalFatG; }

    public BigDecimal getSubtotalFiberG() { return subtotalFiberG; }
    public void setSubtotalFiberG(BigDecimal subtotalFiberG) { this.subtotalFiberG = subtotalFiberG; }
}
