package com.fitkit.domain.entity;

import com.fitkit.domain.enums.MealType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "meal_entries")
public class MealEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_log_id", nullable = false)
    private MealLog mealLog;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", nullable = false, length = 30)
    private MealType mealType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id")
    private FoodItem food;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;

    @Column(name = "food_name", nullable = false, length = 255)
    private String foodName;

    @Column(name = "food_state", nullable = false, length = 50)
    private String foodState;

    @Column(name = "portion_description", nullable = false, length = 150)
    private String portionDescription;

    @Column(name = "portion_gram_weight", precision = 8, scale = 2, nullable = false)
    private BigDecimal portionGramWeight;

    @Column(name = "quantity_multiplier", precision = 6, scale = 2, nullable = false)
    private BigDecimal quantityMultiplier = BigDecimal.ONE;

    @Column(name = "total_gram_weight", precision = 8, scale = 2, nullable = false)
    private BigDecimal totalGramWeight;

    // Snapshot nutrient values (Authoritative historical record)
    @Column(name = "energy_kcal", precision = 8, scale = 2, nullable = false)
    private BigDecimal energyKcal;

    @Column(name = "energy_kj", precision = 8, scale = 2)
    private BigDecimal energyKj;

    @Column(name = "protein_g", precision = 8, scale = 2, nullable = false)
    private BigDecimal proteinG;

    @Column(name = "carbohydrates_g", precision = 8, scale = 2, nullable = false)
    private BigDecimal carbohydratesG;

    @Column(name = "fat_g", precision = 8, scale = 2, nullable = false)
    private BigDecimal fatG;

    @Column(name = "fiber_g", precision = 8, scale = 2, nullable = false)
    private BigDecimal fiberG = BigDecimal.ZERO;

    @Column(name = "created_at", insertable = true, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public MealEntry() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public MealLog getMealLog() { return mealLog; }
    public void setMealLog(MealLog mealLog) { this.mealLog = mealLog; }

    public MealType getMealType() { return mealType; }
    public void setMealType(MealType mealType) { this.mealType = mealType; }

    public FoodItem getFood() { return food; }
    public void setFood(FoodItem food) { this.food = food; }

    public Recipe getRecipe() { return recipe; }
    public void setRecipe(Recipe recipe) { this.recipe = recipe; }

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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
