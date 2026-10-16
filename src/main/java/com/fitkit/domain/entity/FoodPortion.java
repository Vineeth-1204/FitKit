package com.fitkit.domain.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "food_portions")
public class FoodPortion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id", nullable = false)
    private FoodItem food;

    @Column(name = "portion_description", length = 150, nullable = false)
    private String portionDescription;

    @Column(name = "gram_weight", precision = 8, scale = 2, nullable = false)
    private BigDecimal gramWeight;

    @Column(name = "is_default_portion")
    private Boolean isDefaultPortion = false;

    @Column(name = "source_note", length = 255)
    private String sourceNote;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public FoodPortion() {}

    public FoodPortion(FoodItem food, String portionDescription, BigDecimal gramWeight, Boolean isDefaultPortion, String sourceNote) {
        this.food = food;
        this.portionDescription = portionDescription;
        this.gramWeight = gramWeight;
        this.isDefaultPortion = isDefaultPortion;
        this.sourceNote = sourceNote;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public FoodItem getFood() { return food; }
    public void setFood(FoodItem food) { this.food = food; }

    public String getPortionDescription() { return portionDescription; }
    public void setPortionDescription(String portionDescription) { this.portionDescription = portionDescription; }

    public BigDecimal getGramWeight() { return gramWeight; }
    public void setGramWeight(BigDecimal gramWeight) { this.gramWeight = gramWeight; }

    public Boolean getDefaultPortion() { return isDefaultPortion; }
    public void setDefaultPortion(Boolean defaultPortion) { isDefaultPortion = defaultPortion; }

    public String getSourceNote() { return sourceNote; }
    public void setSourceNote(String sourceNote) { this.sourceNote = sourceNote; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
