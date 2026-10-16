package com.fitkit.dto;

import java.math.BigDecimal;

public class UpdateMealEntryRequestDto {
    private BigDecimal portionGramWeight;
    private String portionDescription;
    private BigDecimal quantityMultiplier;

    public UpdateMealEntryRequestDto() {}

    public BigDecimal getPortionGramWeight() { return portionGramWeight; }
    public void setPortionGramWeight(BigDecimal portionGramWeight) { this.portionGramWeight = portionGramWeight; }

    public String getPortionDescription() { return portionDescription; }
    public void setPortionDescription(String portionDescription) { this.portionDescription = portionDescription; }

    public BigDecimal getQuantityMultiplier() { return quantityMultiplier; }
    public void setQuantityMultiplier(BigDecimal quantityMultiplier) { this.quantityMultiplier = quantityMultiplier; }
}
