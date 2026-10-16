package com.fitkit.dto;

import java.util.ArrayList;
import java.util.List;

public class FoodDetailDto extends FoodSummaryDto {
    private String sourceRecordId;
    private String sourceCitation;
    private String sourceLicense;
    private List<FoodNutrientDto> allNutrients = new ArrayList<>();
    private List<FoodPortionDto> portions = new ArrayList<>();

    public FoodDetailDto() {}

    public String getSourceRecordId() { return sourceRecordId; }
    public void setSourceRecordId(String sourceRecordId) { this.sourceRecordId = sourceRecordId; }

    public String getSourceCitation() { return sourceCitation; }
    public void setSourceCitation(String sourceCitation) { this.sourceCitation = sourceCitation; }

    public String getSourceLicense() { return sourceLicense; }
    public void setSourceLicense(String sourceLicense) { this.sourceLicense = sourceLicense; }

    public List<FoodNutrientDto> getAllNutrients() { return allNutrients; }
    public void setAllNutrients(List<FoodNutrientDto> allNutrients) { this.allNutrients = allNutrients; }

    public List<FoodPortionDto> getPortions() { return portions; }
    public void setPortions(List<FoodPortionDto> portions) { this.portions = portions; }
}
