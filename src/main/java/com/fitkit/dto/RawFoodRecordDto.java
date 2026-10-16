package com.fitkit.dto;

import com.fitkit.domain.enums.FoodState;
import java.math.BigDecimal;
import java.util.List;

public class RawFoodRecordDto {
    private String sourceId;
    private String sourceRecordId;
    private String foodCode;
    private String name;
    private String scientificName;
    private String foodGroup;
    private FoodState foodState;
    private String brandName;
    private String barcode;
    
    // Raw inputs
    private String rawEnergyValue; // e.g. "1480 kJ" or "365 kcal"
    private BigDecimal energyKj;
    private BigDecimal energyKcal;
    private BigDecimal proteinG;
    private BigDecimal availableCarbsG;
    private BigDecimal fatG;
    private BigDecimal fiberG;
    private BigDecimal moistureG;
    private BigDecimal ashG;
    private String micronutrientsJson;

    // Portion measures
    private List<PortionDto> portions;

    public RawFoodRecordDto() {}

    public static class PortionDto {
        private String description;
        private BigDecimal gramWeight;
        private Boolean isDefault;

        public PortionDto() {}
        public PortionDto(String description, BigDecimal gramWeight, Boolean isDefault) {
            this.description = description;
            this.gramWeight = gramWeight;
            this.isDefault = isDefault;
        }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public BigDecimal getGramWeight() { return gramWeight; }
        public void setGramWeight(BigDecimal gramWeight) { this.gramWeight = gramWeight; }

        public Boolean getDefault() { return isDefault; }
        public void setDefault(Boolean aDefault) { isDefault = aDefault; }
    }

    public String getSourceId() { return sourceId; }
    public void setSourceId(String sourceId) { this.sourceId = sourceId; }

    public String getSourceRecordId() { return sourceRecordId; }
    public void setSourceRecordId(String sourceRecordId) { this.sourceRecordId = sourceRecordId; }

    public String getFoodCode() { return foodCode; }
    public void setFoodCode(String foodCode) { this.foodCode = foodCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getScientificName() { return scientificName; }
    public void setScientificName(String scientificName) { this.scientificName = scientificName; }

    public String getFoodGroup() { return foodGroup; }
    public void setFoodGroup(String foodGroup) { this.foodGroup = foodGroup; }

    public FoodState getFoodState() { return foodState; }
    public void setFoodState(FoodState foodState) { this.foodState = foodState; }

    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public String getRawEnergyValue() { return rawEnergyValue; }
    public void setRawEnergyValue(String rawEnergyValue) { this.rawEnergyValue = rawEnergyValue; }

    public BigDecimal getEnergyKj() { return energyKj; }
    public void setEnergyKj(BigDecimal energyKj) { this.energyKj = energyKj; }

    public BigDecimal getEnergyKcal() { return energyKcal; }
    public void setEnergyKcal(BigDecimal energyKcal) { this.energyKcal = energyKcal; }

    public BigDecimal getProteinG() { return proteinG; }
    public void setProteinG(BigDecimal proteinG) { this.proteinG = proteinG; }

    public BigDecimal getAvailableCarbsG() { return availableCarbsG; }
    public void setAvailableCarbsG(BigDecimal availableCarbsG) { this.availableCarbsG = availableCarbsG; }

    public BigDecimal getFatG() { return fatG; }
    public void setFatG(BigDecimal fatG) { this.fatG = fatG; }

    public BigDecimal getFiberG() { return fiberG; }
    public void setFiberG(BigDecimal fiberG) { this.fiberG = fiberG; }

    public BigDecimal getDietaryFiberG() { return fiberG; }
    public void setDietaryFiberG(BigDecimal fiberG) { this.fiberG = fiberG; }

    public BigDecimal getMoistureG() { return moistureG; }
    public void setMoistureG(BigDecimal moistureG) { this.moistureG = moistureG; }

    public BigDecimal getAshG() { return ashG; }
    public void setAshG(BigDecimal ashG) { this.ashG = ashG; }

    public String getMicronutrientsJson() { return micronutrientsJson; }
    public void setMicronutrientsJson(String micronutrientsJson) { this.micronutrientsJson = micronutrientsJson; }

    public List<PortionDto> getPortions() { return portions; }
    public void setPortions(List<PortionDto> portions) { this.portions = portions; }
}
