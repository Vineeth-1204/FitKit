package com.fitkit.domain.entity;

import com.fitkit.domain.enums.CookingMethod;
import com.fitkit.domain.enums.UncertaintyRating;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recipes")
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recipe_code", length = 100, nullable = false, unique = true)
    private String recipeCode;
 
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id", unique = true)
    private FoodItem food;

    @Column(name = "user_id", length = 191)
    private String userId;

    @Column(name = "is_custom", nullable = false)
    private Boolean isCustom = false;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "cuisine_region", length = 100)
    private String cuisineRegion;

    @Column(name = "total_raw_weight_g", precision = 8, scale = 2, nullable = false)
    private BigDecimal totalRawWeightG;

    @Column(name = "cooking_yield_factor", precision = 5, scale = 3, nullable = false)
    private BigDecimal cookingYieldFactor;

    @Column(name = "cooked_weight_g", precision = 8, scale = 2, nullable = false)
    private BigDecimal cookedWeightG;

    @Column(name = "servings_yield", precision = 5, scale = 2, nullable = false)
    private BigDecimal servingsYield;

    @Column(name = "serving_weight_g", precision = 8, scale = 2, nullable = false)
    private BigDecimal servingWeightG;

    @Enumerated(EnumType.STRING)
    @Column(name = "cooking_method", length = 50, nullable = false)
    private CookingMethod cookingMethod;

    @Column(name = "preparation_notes", columnDefinition = "TEXT")
    private String preparationNotes;

    @Enumerated(EnumType.STRING)
    @Column(name = "uncertainty_rating", length = 50, nullable = false)
    private UncertaintyRating uncertaintyRating = UncertaintyRating.MODERATE_HOME_VARIANCE;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<RecipeIngredient> ingredients = new ArrayList<>();

    public Recipe() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRecipeCode() { return recipeCode; }
    public void setRecipeCode(String recipeCode) { this.recipeCode = recipeCode; }

    public FoodItem getFood() { return food; }
    public void setFood(FoodItem food) { this.food = food; }

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

    public CookingMethod getCookingMethod() { return cookingMethod; }
    public void setCookingMethod(CookingMethod cookingMethod) { this.cookingMethod = cookingMethod; }

    public String getPreparationNotes() { return preparationNotes; }
    public void setPreparationNotes(String preparationNotes) { this.preparationNotes = preparationNotes; }

    public UncertaintyRating getUncertaintyRating() { return uncertaintyRating; }
    public void setUncertaintyRating(UncertaintyRating uncertaintyRating) { this.uncertaintyRating = uncertaintyRating; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Boolean getIsCustom() { return isCustom; }
    public void setIsCustom(Boolean isCustom) { this.isCustom = isCustom; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<RecipeIngredient> getIngredients() { return ingredients; }
    public void setIngredients(List<RecipeIngredient> ingredients) { this.ingredients = ingredients; }
}
