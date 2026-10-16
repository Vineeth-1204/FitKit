package com.fitkit.dto;

import java.math.BigDecimal;

public class FoodPortionDto {
    private Long id;
    private String portionDescription;
    private BigDecimal gramWeight;
    private Boolean isDefaultPortion;
    private String sourceNote;

    public FoodPortionDto() {}

    public FoodPortionDto(Long id, String portionDescription, BigDecimal gramWeight, Boolean isDefaultPortion, String sourceNote) {
        this.id = id;
        this.portionDescription = portionDescription;
        this.gramWeight = gramWeight;
        this.isDefaultPortion = isDefaultPortion;
        this.sourceNote = sourceNote;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPortionDescription() { return portionDescription; }
    public void setPortionDescription(String portionDescription) { this.portionDescription = portionDescription; }

    public BigDecimal getGramWeight() { return gramWeight; }
    public void setGramWeight(BigDecimal gramWeight) { this.gramWeight = gramWeight; }

    public Boolean getDefaultPortion() { return isDefaultPortion; }
    public void setDefaultPortion(Boolean defaultPortion) { isDefaultPortion = defaultPortion; }

    public String getSourceNote() { return sourceNote; }
    public void setSourceNote(String sourceNote) { this.sourceNote = sourceNote; }
}
