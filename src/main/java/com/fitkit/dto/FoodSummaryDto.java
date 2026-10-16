package com.fitkit.dto;

import java.math.BigDecimal;

public class FoodSummaryDto {
    private Long id;
    private String foodCode;
    private String name;
    private String scientificName;
    private String foodGroup;
    private String foodState;
    private String brandName;
    private String barcode;
    private Boolean isVerified;
    private String sourceName;
    private String sourcePublisher;
    private BigDecimal energyKcal;
    private BigDecimal energyKj;
    private BigDecimal proteinG;
    private BigDecimal availableCarbohydratesG;
    private BigDecimal totalFatG;
    private BigDecimal dietaryFiberG;
    private int portionCount;
    private String qualityStatus;

    public FoodSummaryDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFoodCode() { return foodCode; }
    public void setFoodCode(String foodCode) { this.foodCode = foodCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getScientificName() { return scientificName; }
    public void setScientificName(String scientificName) { this.scientificName = scientificName; }

    public String getFoodGroup() { return foodGroup; }
    public void setFoodGroup(String foodGroup) { this.foodGroup = foodGroup; }

    public String getFoodState() { return foodState; }
    public void setFoodState(String foodState) { this.foodState = foodState; }

    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public Boolean getVerified() { return isVerified; }
    public void setVerified(Boolean verified) { isVerified = verified; }

    public String getSourceName() { return sourceName; }
    public void setSourceName(String sourceName) { this.sourceName = sourceName; }

    public String getSourcePublisher() { return sourcePublisher; }
    public void setSourcePublisher(String sourcePublisher) { this.sourcePublisher = sourcePublisher; }

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

    public int getPortionCount() { return portionCount; }
    public void setPortionCount(int portionCount) { this.portionCount = portionCount; }

    public String getQualityStatus() { return qualityStatus; }
    public void setQualityStatus(String qualityStatus) { this.qualityStatus = qualityStatus; }

    private java.util.List<String> aliases = new java.util.ArrayList<>();
    private String matchedAlias;
    private String micronutrientsJson;

    public java.util.List<String> getAliases() { return aliases; }
    public void setAliases(java.util.List<String> aliases) { this.aliases = aliases; }

    public String getMatchedAlias() { return matchedAlias; }
    public void setMatchedAlias(String matchedAlias) { this.matchedAlias = matchedAlias; }

    public String getMicronutrientsJson() { return micronutrientsJson; }
    public void setMicronutrientsJson(String micronutrientsJson) { this.micronutrientsJson = micronutrientsJson; }
}
