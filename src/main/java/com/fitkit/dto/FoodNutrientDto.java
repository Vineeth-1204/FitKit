package com.fitkit.dto;

import java.math.BigDecimal;

public class FoodNutrientDto {
    private String basis;
    private BigDecimal energyKcal;
    private BigDecimal energyKj;
    private BigDecimal proteinG;
    private BigDecimal availableCarbohydratesG;
    private BigDecimal totalFatG;
    private BigDecimal dietaryFiberG;
    private BigDecimal moistureWaterG;
    private BigDecimal ashG;
    private String qualityStatus;
    private String correctionNotes;
    private String rawSourceEnergy;
    private String micronutrientsJson;

    public FoodNutrientDto() {}

    public String getBasis() { return basis; }
    public void setBasis(String basis) { this.basis = basis; }

    public BigDecimal getEnergyKcal() { return energyKcal; }
    public void setEnergyKcal(BigDecimal energyKcal) { this.energyKcal = energyKcal; }

    public BigDecimal getEnergyKj() { return energyKj; }
    public void setEnergyKj(BigDecimal energyKj) { this.energyKj = energyKj; }

    public BigDecimal getProteinG() { return proteinG; }
    public void setProteinG(BigDecimal proteinG) { this.proteinG = proteinG; }

    public BigDecimal getAvailableCarbohydratesG() { return availableCarbohydratesG; }
    public void setAvailableCarbohydratesG(BigDecimal availableCarbohydratesG) { this.availableCarbohydratesG = availableCarbohydratesG; }

    public BigDecimal getTotalFatG() { return totalFatG; }
    public void setTotalFatG(BigDecimal totalFatG) { this.totalFatG = totalFatG; }

    public BigDecimal getDietaryFiberG() { return dietaryFiberG; }
    public void setDietaryFiberG(BigDecimal dietaryFiberG) { this.dietaryFiberG = dietaryFiberG; }

    public BigDecimal getMoistureWaterG() { return moistureWaterG; }
    public void setMoistureWaterG(BigDecimal moistureWaterG) { this.moistureWaterG = moistureWaterG; }

    public BigDecimal getAshG() { return ashG; }
    public void setAshG(BigDecimal ashG) { this.ashG = ashG; }

    public String getQualityStatus() { return qualityStatus; }
    public void setQualityStatus(String qualityStatus) { this.qualityStatus = qualityStatus; }

    public String getCorrectionNotes() { return correctionNotes; }
    public void setCorrectionNotes(String correctionNotes) { this.correctionNotes = correctionNotes; }

    public String getRawSourceEnergy() { return rawSourceEnergy; }
    public void setRawSourceEnergy(String rawSourceEnergy) { this.rawSourceEnergy = rawSourceEnergy; }

    public String getMicronutrientsJson() { return micronutrientsJson; }
    public void setMicronutrientsJson(String micronutrientsJson) { this.micronutrientsJson = micronutrientsJson; }
}
