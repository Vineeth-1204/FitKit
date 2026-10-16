package com.fitkit.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "food_aliases")
public class FoodAlias {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id", nullable = false)
    private FoodItem food;

    @Column(name = "alias_name", length = 150, nullable = false)
    private String aliasName;

    @Column(name = "language_or_dialect", length = 50)
    private String languageOrDialect;

    @Column(name = "is_primary")
    private Boolean isPrimary = false;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public FoodAlias() {}

    public FoodAlias(FoodItem food, String aliasName, String languageOrDialect, Boolean isPrimary) {
        this.food = food;
        this.aliasName = aliasName;
        this.languageOrDialect = languageOrDialect;
        this.isPrimary = isPrimary != null ? isPrimary : false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public FoodItem getFood() {
        return food;
    }

    public void setFood(FoodItem food) {
        this.food = food;
    }

    public String getAliasName() {
        return aliasName;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    public String getLanguageOrDialect() {
        return languageOrDialect;
    }

    public void setLanguageOrDialect(String languageOrDialect) {
        this.languageOrDialect = languageOrDialect;
    }

    public Boolean getPrimary() {
        return isPrimary;
    }

    public void setPrimary(Boolean primary) {
        isPrimary = primary;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
