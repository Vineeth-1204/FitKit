package com.fitkit.domain.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "recipe_ingredients")
public class RecipeIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "food_id", nullable = false)
    private FoodItem food;

    @Column(name = "raw_quantity_g", precision = 8, scale = 2, nullable = false)
    private BigDecimal rawQuantityG;

    @Column(name = "unit_measure", length = 50, nullable = false)
    private String unitMeasure = "g";

    @Column(name = "retention_factor_override", precision = 5, scale = 3)
    private BigDecimal retentionFactorOverride;

    @Column(name = "notes", length = 255)
    private String notes;

    public RecipeIngredient() {}

    public RecipeIngredient(Recipe recipe, FoodItem food, BigDecimal rawQuantityG, String unitMeasure, BigDecimal retentionFactorOverride, String notes) {
        this.recipe = recipe;
        this.food = food;
        this.rawQuantityG = rawQuantityG;
        this.unitMeasure = unitMeasure;
        this.retentionFactorOverride = retentionFactorOverride;
        this.notes = notes;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Recipe getRecipe() { return recipe; }
    public void setRecipe(Recipe recipe) { this.recipe = recipe; }

    public FoodItem getFood() { return food; }
    public void setFood(FoodItem food) { this.food = food; }

    public BigDecimal getRawQuantityG() { return rawQuantityG; }
    public void setRawQuantityG(BigDecimal rawQuantityG) { this.rawQuantityG = rawQuantityG; }

    public String getUnitMeasure() { return unitMeasure; }
    public void setUnitMeasure(String unitMeasure) { this.unitMeasure = unitMeasure; }

    public BigDecimal getRetentionFactorOverride() { return retentionFactorOverride; }
    public void setRetentionFactorOverride(BigDecimal retentionFactorOverride) { this.retentionFactorOverride = retentionFactorOverride; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
