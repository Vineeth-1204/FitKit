package com.fitkit.controller;

import com.fitkit.domain.entity.FoodItem;
import com.fitkit.domain.entity.FoodNutrient;
import com.fitkit.domain.entity.FoodPortion;
import com.fitkit.domain.enums.FoodState;
import com.fitkit.domain.enums.NutrientBasis;
import com.fitkit.dto.FoodDetailDto;
import com.fitkit.dto.FoodNutrientDto;
import com.fitkit.dto.FoodPortionDto;
import com.fitkit.dto.FoodSummaryDto;
import com.fitkit.repository.FoodItemRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/foods")
@CrossOrigin(origins = "*")
public class FoodCatalogController {

    private final FoodItemRepository foodItemRepository;
    private final com.fitkit.service.FoodSearchService foodSearchService;

    public FoodCatalogController(FoodItemRepository foodItemRepository, com.fitkit.service.FoodSearchService foodSearchService) {
        this.foodItemRepository = foodItemRepository;
        this.foodSearchService = foodSearchService;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<FoodSummaryDto>> getFoods(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String group,
            @RequestParam(required = false) String quality,
            @RequestParam(required = false) String sort) {

        List<FoodSummaryDto> dtos = foodSearchService.searchAndFilterFoods(query, state, group, quality, sort);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<FoodDetailDto> getFoodById(@PathVariable Long id) {
        return foodItemRepository.findByIdWithDetails(id)
                .map(this::mapToDetailDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/groups")
    @Transactional(readOnly = true)
    public ResponseEntity<List<String>> getFoodGroups() {
        return ResponseEntity.ok(foodItemRepository.findDistinctFoodGroups());
    }

    @GetMapping("/states")
    public ResponseEntity<List<String>> getFoodStates() {
        List<String> states = Arrays.stream(FoodState.values())
                .map(Enum::name)
                .collect(Collectors.toList());
        return ResponseEntity.ok(states);
    }

    @GetMapping("/qualities")
    public ResponseEntity<List<String>> getQualityStatuses() {
        List<String> qualities = Arrays.stream(com.fitkit.domain.enums.QualityStatus.values())
                .map(Enum::name)
                .collect(Collectors.toList());
        return ResponseEntity.ok(qualities);
    }

    private FoodSummaryDto mapToSummaryDto(FoodItem item) {
        FoodSummaryDto dto = new FoodSummaryDto();
        dto.setId(item.getId());
        dto.setFoodCode(item.getFoodCode());
        dto.setName(item.getName());
        dto.setScientificName(item.getScientificName());
        dto.setFoodGroup(item.getFoodGroup());
        dto.setFoodState(item.getFoodState() != null ? item.getFoodState().name() : null);
        dto.setBrandName(item.getBrandName());
        dto.setBarcode(item.getBarcode());
        dto.setVerified(item.getVerified());

        if (item.getSource() != null) {
            dto.setSourceName(item.getSource().getDatasetName());
            dto.setSourcePublisher(item.getSource().getPublisher());
        }

        dto.setPortionCount(item.getPortions() != null ? item.getPortions().size() : 0);

        FoodNutrient nut100g = item.getNutrients().stream()
                .filter(n -> n.getBasis() == NutrientBasis.PER_100G)
                .findFirst()
                .orElse(null);

        if (nut100g != null) {
            dto.setEnergyKcal(nut100g.getEnergyKcal());
            dto.setEnergyKj(nut100g.getEnergyKj());
            dto.setProteinG(nut100g.getProteinG());
            dto.setAvailableCarbohydratesG(nut100g.getAvailableCarbohydratesG());
            dto.setTotalFatG(nut100g.getTotalFatG());
            dto.setDietaryFiberG(nut100g.getDietaryFiberG());
            dto.setQualityStatus(nut100g.getQualityStatus() != null ? nut100g.getQualityStatus().name() : null);
            dto.setMicronutrientsJson(nut100g.getMicronutrientsJson());
        }

        if (item.getAliases() != null && !item.getAliases().isEmpty()) {
            dto.setAliases(item.getAliases().stream()
                    .map(com.fitkit.domain.entity.FoodAlias::getAliasName)
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    private FoodDetailDto mapToDetailDto(FoodItem item) {
        FoodDetailDto dto = new FoodDetailDto();
        dto.setId(item.getId());
        dto.setFoodCode(item.getFoodCode());
        dto.setName(item.getName());
        dto.setScientificName(item.getScientificName());
        dto.setFoodGroup(item.getFoodGroup());
        dto.setFoodState(item.getFoodState() != null ? item.getFoodState().name() : null);
        dto.setBrandName(item.getBrandName());
        dto.setBarcode(item.getBarcode());
        dto.setVerified(item.getVerified());
        dto.setSourceRecordId(item.getSourceRecordId());

        if (item.getSource() != null) {
            dto.setSourceName(item.getSource().getDatasetName());
            dto.setSourcePublisher(item.getSource().getPublisher());
            dto.setSourceCitation(item.getSource().getCitationText());
            dto.setSourceLicense(item.getSource().getLicenseType());
        }

        FoodNutrient nut100g = item.getNutrients().stream()
                .filter(n -> n.getBasis() == NutrientBasis.PER_100G)
                .findFirst()
                .orElse(null);

        if (nut100g != null) {
            dto.setEnergyKcal(nut100g.getEnergyKcal());
            dto.setEnergyKj(nut100g.getEnergyKj());
            dto.setProteinG(nut100g.getProteinG());
            dto.setAvailableCarbohydratesG(nut100g.getAvailableCarbohydratesG());
            dto.setTotalFatG(nut100g.getTotalFatG());
            dto.setDietaryFiberG(nut100g.getDietaryFiberG());
            dto.setQualityStatus(nut100g.getQualityStatus() != null ? nut100g.getQualityStatus().name() : null);
        }

        List<FoodNutrientDto> nutrientDtos = new ArrayList<>();
        for (FoodNutrient n : item.getNutrients()) {
            FoodNutrientDto ndto = new FoodNutrientDto();
            ndto.setBasis(n.getBasis() != null ? n.getBasis().name() : null);
            ndto.setEnergyKcal(n.getEnergyKcal());
            ndto.setEnergyKj(n.getEnergyKj());
            ndto.setProteinG(n.getProteinG());
            ndto.setAvailableCarbohydratesG(n.getAvailableCarbohydratesG());
            ndto.setTotalFatG(n.getTotalFatG());
            ndto.setDietaryFiberG(n.getDietaryFiberG());
            ndto.setMoistureWaterG(n.getMoistureWaterG());
            ndto.setAshG(n.getAshG());
            ndto.setQualityStatus(n.getQualityStatus() != null ? n.getQualityStatus().name() : null);
            ndto.setCorrectionNotes(n.getCorrectionNotes());
            ndto.setRawSourceEnergy(n.getRawSourceEnergy());
            ndto.setMicronutrientsJson(n.getMicronutrientsJson());
            nutrientDtos.add(ndto);
        }
        dto.setAllNutrients(nutrientDtos);

        List<FoodPortionDto> portionDtos = new ArrayList<>();
        for (FoodPortion p : item.getPortions()) {
            portionDtos.add(new FoodPortionDto(
                    p.getId(),
                    p.getPortionDescription(),
                    p.getGramWeight(),
                    p.getDefaultPortion(),
                    p.getSourceNote()
            ));
        }
        dto.setPortions(portionDtos);
        dto.setPortionCount(portionDtos.size());

        if (item.getAliases() != null && !item.getAliases().isEmpty()) {
            dto.setAliases(item.getAliases().stream()
                    .map(com.fitkit.domain.entity.FoodAlias::getAliasName)
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toList()));
        }

        return dto;
    }
}
