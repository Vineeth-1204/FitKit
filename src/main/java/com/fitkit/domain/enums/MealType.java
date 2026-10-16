package com.fitkit.domain.enums;

public enum MealType {
    BREAKFAST("Breakfast"),
    LUNCH("Lunch"),
    DINNER("Dinner"),
    SNACKS("Snacks");

    private final String displayName;

    MealType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static MealType fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return BREAKFAST;
        }
        for (MealType mt : MealType.values()) {
            if (mt.name().equalsIgnoreCase(text.trim()) || mt.displayName.equalsIgnoreCase(text.trim())) {
                return mt;
            }
        }
        return BREAKFAST;
    }
}
