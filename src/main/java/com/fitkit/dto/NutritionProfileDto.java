package com.fitkit.dto;

import jakarta.validation.constraints.*;

public class NutritionProfileDto {
    private boolean complete;

    @NotNull @Min(18) @Max(100)
    private Integer age;

    @NotBlank @Pattern(regexp = "FEMALE|MALE|UNSPECIFIED")
    private String sexForEstimate;

    @NotNull @Min(120) @Max(230)
    private Integer heightCm;

    @NotNull @DecimalMin("35.0") @DecimalMax("300.0")
    private Double weightKg;

    @NotBlank @Pattern(regexp = "SEDENTARY|LIGHT|MODERATE|HIGH")
    private String activityLevel;

    @NotBlank @Pattern(regexp = "LOSE|MAINTAIN|GAIN")
    private String goal;

    private Integer dailyCalories;
    private Double proteinG;
    private Double carbsG;
    private Double fatG;
    private Double fiberG;

    public boolean isComplete() { return complete; }
    public void setComplete(boolean complete) { this.complete = complete; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public String getSexForEstimate() { return sexForEstimate; }
    public void setSexForEstimate(String sexForEstimate) { this.sexForEstimate = sexForEstimate; }
    public Integer getHeightCm() { return heightCm; }
    public void setHeightCm(Integer heightCm) { this.heightCm = heightCm; }
    public Double getWeightKg() { return weightKg; }
    public void setWeightKg(Double weightKg) { this.weightKg = weightKg; }
    public String getActivityLevel() { return activityLevel; }
    public void setActivityLevel(String activityLevel) { this.activityLevel = activityLevel; }
    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }
    public Integer getDailyCalories() { return dailyCalories; }
    public void setDailyCalories(Integer dailyCalories) { this.dailyCalories = dailyCalories; }
    public Double getProteinG() { return proteinG; }
    public void setProteinG(Double proteinG) { this.proteinG = proteinG; }
    public Double getCarbsG() { return carbsG; }
    public void setCarbsG(Double carbsG) { this.carbsG = carbsG; }
    public Double getFatG() { return fatG; }
    public void setFatG(Double fatG) { this.fatG = fatG; }
    public Double getFiberG() { return fiberG; }
    public void setFiberG(Double fiberG) { this.fiberG = fiberG; }
}
