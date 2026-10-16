package com.fitkit.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class DailyMealLogDto {
    private Long id;
    private String userId;
    private LocalDate date;
    private String notes;

    private BigDecimal totalCalories = BigDecimal.ZERO;
    private BigDecimal totalProteinG = BigDecimal.ZERO;
    private BigDecimal totalCarbsG = BigDecimal.ZERO;
    private BigDecimal totalFatG = BigDecimal.ZERO;
    private BigDecimal totalFiberG = BigDecimal.ZERO;

    private Integer targetCalories;
    private BigDecimal targetProteinG;
    private BigDecimal targetCarbsG;
    private BigDecimal targetFatG;
    private BigDecimal targetFiberG;

    private BigDecimal remainingCalories;
    private Integer caloriePercentage;

    private Map<String, MealSectionDto> meals = new HashMap<>();

    public DailyMealLogDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public BigDecimal getTotalCalories() { return totalCalories; }
    public void setTotalCalories(BigDecimal totalCalories) { this.totalCalories = totalCalories; }

    public BigDecimal getTotalProteinG() { return totalProteinG; }
    public void setTotalProteinG(BigDecimal totalProteinG) { this.totalProteinG = totalProteinG; }

    public BigDecimal getTotalCarbsG() { return totalCarbsG; }
    public void setTotalCarbsG(BigDecimal totalCarbsG) { this.totalCarbsG = totalCarbsG; }

    public BigDecimal getTotalFatG() { return totalFatG; }
    public void setTotalFatG(BigDecimal totalFatG) { this.totalFatG = totalFatG; }

    public BigDecimal getTotalFiberG() { return totalFiberG; }
    public void setTotalFiberG(BigDecimal totalFiberG) { this.totalFiberG = totalFiberG; }

    public Integer getTargetCalories() { return targetCalories; }
    public void setTargetCalories(Integer targetCalories) { this.targetCalories = targetCalories; }

    public BigDecimal getTargetProteinG() { return targetProteinG; }
    public void setTargetProteinG(BigDecimal targetProteinG) { this.targetProteinG = targetProteinG; }

    public BigDecimal getTargetCarbsG() { return targetCarbsG; }
    public void setTargetCarbsG(BigDecimal targetCarbsG) { this.targetCarbsG = targetCarbsG; }

    public BigDecimal getTargetFatG() { return targetFatG; }
    public void setTargetFatG(BigDecimal targetFatG) { this.targetFatG = targetFatG; }

    public BigDecimal getTargetFiberG() { return targetFiberG; }
    public void setTargetFiberG(BigDecimal targetFiberG) { this.targetFiberG = targetFiberG; }

    public BigDecimal getRemainingCalories() { return remainingCalories; }
    public void setRemainingCalories(BigDecimal remainingCalories) { this.remainingCalories = remainingCalories; }

    public Integer getCaloriePercentage() { return caloriePercentage; }
    public void setCaloriePercentage(Integer caloriePercentage) { this.caloriePercentage = caloriePercentage; }

    public Map<String, MealSectionDto> getMeals() { return meals; }
    public void setMeals(Map<String, MealSectionDto> meals) { this.meals = meals; }
}
