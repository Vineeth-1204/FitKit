package com.fitkit.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class ClientDiaryMigrationDto {
    private LocalDate date;
    private Map<String, List<MigratedItem>> meals;

    public static class MigratedItem {
        private String id;
        private Long foodId;
        private String name;
        private String portionDesc;
        private BigDecimal gramWeight;
        private BigDecimal calories;
        private BigDecimal protein;
        private BigDecimal carbs;
        private BigDecimal fat;
        private BigDecimal fiber;
        private String state;

        public MigratedItem() {}

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public Long getFoodId() { return foodId; }
        public void setFoodId(Long foodId) { this.foodId = foodId; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getPortionDesc() { return portionDesc; }
        public void setPortionDesc(String portionDesc) { this.portionDesc = portionDesc; }

        public BigDecimal getGramWeight() { return gramWeight; }
        public void setGramWeight(BigDecimal gramWeight) { this.gramWeight = gramWeight; }

        public BigDecimal getCalories() { return calories; }
        public void setCalories(BigDecimal calories) { this.calories = calories; }

        public BigDecimal getProtein() { return protein; }
        public void setProtein(BigDecimal protein) { this.protein = protein; }

        public BigDecimal getCarbs() { return carbs; }
        public void setCarbs(BigDecimal carbs) { this.carbs = carbs; }

        public BigDecimal getFat() { return fat; }
        public void setFat(BigDecimal fat) { this.fat = fat; }

        public BigDecimal getFiber() { return fiber; }
        public void setFiber(BigDecimal fiber) { this.fiber = fiber; }

        public String getState() { return state; }
        public void setState(String state) { this.state = state; }
    }

    public ClientDiaryMigrationDto() {}

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Map<String, List<MigratedItem>> getMeals() { return meals; }
    public void setMeals(Map<String, List<MigratedItem>> meals) { this.meals = meals; }
}
