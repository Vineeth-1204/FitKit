package com.fitkit.domain.entity;

import com.fitkit.domain.enums.NutrientBasis;
import com.fitkit.domain.enums.QualityStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "food_nutrients")
public class FoodNutrient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id", nullable = false)
    private FoodItem food;

    @Enumerated(EnumType.STRING)
    @Column(name = "basis", length = 30, nullable = false)
    private NutrientBasis basis = NutrientBasis.PER_100G;

    @Column(name = "energy_kcal", precision = 8, scale = 2)
    private BigDecimal energyKcal;

    @Column(name = "energy_kj", precision = 8, scale = 2)
    private BigDecimal energyKj;

    @Column(name = "protein_g", precision = 8, scale = 2)
    private BigDecimal proteinG;

    @Column(name = "available_carbohydrates_g", precision = 8, scale = 2)
    private BigDecimal availableCarbohydratesG;

    @Column(name = "total_fat_g", precision = 8, scale = 2)
    private BigDecimal totalFatG;

    @Column(name = "dietary_fiber_g", precision = 8, scale = 2)
    private BigDecimal dietaryFiberG;

    @Column(name = "moisture_water_g", precision = 8, scale = 2)
    private BigDecimal moistureWaterG;

    @Column(name = "ash_g", precision = 8, scale = 2)
    private BigDecimal ashG;

    @Column(name = "micronutrients_json", columnDefinition = "TEXT")
    private String micronutrientsJson;

    @Column(name = "raw_source_energy", length = 50)
    private String rawSourceEnergy;

    @Enumerated(EnumType.STRING)
    @Column(name = "quality_status", length = 50, nullable = false)
    private QualityStatus qualityStatus = QualityStatus.SOURCE_VERIFIED;

    @Column(name = "correction_notes", columnDefinition = "TEXT")
    private String correctionNotes;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public FoodNutrient() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public FoodItem getFood() { return food; }
    public void setFood(FoodItem food) { this.food = food; }

    public NutrientBasis getBasis() { return basis; }
    public void setBasis(NutrientBasis basis) { this.basis = basis; }

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

    public String getMicronutrientsJson() { return micronutrientsJson; }
    public void setMicronutrientsJson(String micronutrientsJson) { this.micronutrientsJson = micronutrientsJson; }

    public String getRawSourceEnergy() { return rawSourceEnergy; }
    public void setRawSourceEnergy(String rawSourceEnergy) { this.rawSourceEnergy = rawSourceEnergy; }

    public QualityStatus getQualityStatus() { return qualityStatus; }
    public void setQualityStatus(QualityStatus qualityStatus) { this.qualityStatus = qualityStatus; }

    public String getCorrectionNotes() { return correctionNotes; }
    public void setCorrectionNotes(String correctionNotes) { this.correctionNotes = correctionNotes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
