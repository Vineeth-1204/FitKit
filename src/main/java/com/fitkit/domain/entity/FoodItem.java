package com.fitkit.domain.entity;

import com.fitkit.domain.enums.FoodState;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "foods")
public class FoodItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "food_code", length = 100, nullable = false, unique = true)
    private String foodCode;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "source_id", nullable = false)
    private SourceProvenance source;

    @Column(name = "source_record_id", length = 100)
    private String sourceRecordId;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "scientific_name", length = 255)
    private String scientificName;

    @Column(name = "food_group", length = 100)
    private String foodGroup;

    @Enumerated(EnumType.STRING)
    @Column(name = "food_state", length = 50, nullable = false)
    private FoodState foodState;

    @Column(name = "brand_name", length = 150)
    private String brandName;

    @Column(name = "barcode", length = 50)
    private String barcode;

    @Column(name = "is_verified")
    private Boolean isVerified = false;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "food", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @org.hibernate.annotations.BatchSize(size = 100)
    private List<FoodNutrient> nutrients = new ArrayList<>();

    @OneToMany(mappedBy = "food", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @org.hibernate.annotations.BatchSize(size = 100)
    private List<FoodPortion> portions = new ArrayList<>();

    @OneToMany(mappedBy = "food", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @org.hibernate.annotations.BatchSize(size = 100)
    private List<FoodAlias> aliases = new ArrayList<>();

    public FoodItem() {}

    public FoodItem(String foodCode, SourceProvenance source, String sourceRecordId, String name,
                    String scientificName, String foodGroup, FoodState foodState, String brandName, String barcode) {
        this.foodCode = foodCode;
        this.source = source;
        this.sourceRecordId = sourceRecordId;
        this.name = name;
        this.scientificName = scientificName;
        this.foodGroup = foodGroup;
        this.foodState = foodState;
        this.brandName = brandName;
        this.barcode = barcode;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFoodCode() { return foodCode; }
    public void setFoodCode(String foodCode) { this.foodCode = foodCode; }

    public SourceProvenance getSource() { return source; }
    public void setSource(SourceProvenance source) { this.source = source; }

    public String getSourceRecordId() { return sourceRecordId; }
    public void setSourceRecordId(String sourceRecordId) { this.sourceRecordId = sourceRecordId; }

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

    public Boolean getVerified() { return isVerified; }
    public void setVerified(Boolean verified) { isVerified = verified; }

    public Boolean getActive() { return isActive; }
    public Boolean getIsActive() { return isActive; }
    public void setActive(Boolean active) { isActive = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<FoodNutrient> getNutrients() { return nutrients; }
    public void setNutrients(List<FoodNutrient> nutrients) { this.nutrients = nutrients; }

    public List<FoodPortion> getPortions() { return portions; }
    public void setPortions(List<FoodPortion> portions) { this.portions = portions; }

    public List<FoodAlias> getAliases() { return aliases; }
    public void setAliases(List<FoodAlias> aliases) { this.aliases = aliases; }
}
