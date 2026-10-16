package com.fitkit.service;

import com.fitkit.domain.entity.FoodAlias;
import com.fitkit.domain.entity.FoodItem;
import com.fitkit.domain.entity.FoodNutrient;
import com.fitkit.domain.enums.NutrientBasis;
import com.fitkit.dto.FoodSummaryDto;
import com.fitkit.repository.FoodItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FoodSearchService {

    private final FoodItemRepository foodItemRepository;

    public FoodSearchService(FoodItemRepository foodItemRepository) {
        this.foodItemRepository = foodItemRepository;
    }

    /**
     * Search and filter foods with deterministic relevance ranking, aliases, and quality indicators.
     */
    @Transactional(readOnly = true)
    public List<FoodSummaryDto> searchAndFilterFoods(String query, String state, String group, String quality, String sort) {
        List<FoodItem> items = foodItemRepository.findAllWithAliasesAndSource();

        // 1. Normalize query
        String normalizedQuery = (query != null) ? query.trim().toLowerCase().replaceAll("\\s+", " ") : "";
        boolean hasQuery = !normalizedQuery.isEmpty();

        // 2. Evaluate matches and ranks
        List<ScoredFood> scoredFoods = new ArrayList<>();

        for (FoodItem item : items) {
            if (item.getActive() != null && !item.getActive()) {
                continue;
            }

            int rank = 100;
            String matchedAlias = null;

            if (hasQuery) {
                String canonicalName = item.getName() != null ? item.getName().trim().toLowerCase() : "";
                String scientificName = item.getScientificName() != null ? item.getScientificName().trim().toLowerCase() : "";
                String foodCode = item.getFoodCode() != null ? item.getFoodCode().trim().toLowerCase() : "";
                String brandName = item.getBrandName() != null ? item.getBrandName().trim().toLowerCase() : "";

                List<String> aliases = (item.getAliases() != null)
                        ? item.getAliases().stream()
                            .map(a -> a.getAliasName() != null ? a.getAliasName().trim().toLowerCase() : "")
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.toList())
                        : Collections.emptyList();

                // Rank 1: Exact canonical-name match
                if (canonicalName.equals(normalizedQuery)) {
                    rank = 1;
                }
                // Rank 2: Exact alias match
                else if (aliases.stream().anyMatch(a -> a.equals(normalizedQuery))) {
                    rank = 2;
                    matchedAlias = aliases.stream().filter(a -> a.equals(normalizedQuery)).findFirst().orElse(null);
                }
                // Rank 3: Canonical name starts with query
                else if (canonicalName.startsWith(normalizedQuery)) {
                    rank = 3;
                }
                // Rank 4: Alias starts with query
                else if (aliases.stream().anyMatch(a -> a.startsWith(normalizedQuery))) {
                    rank = 4;
                    matchedAlias = aliases.stream().filter(a -> a.startsWith(normalizedQuery)).findFirst().orElse(null);
                }
                // Rank 5: Partial canonical name match
                else if (canonicalName.contains(normalizedQuery)) {
                    rank = 5;
                }
                // Rank 6: Partial alias or other text fields match
                else if (aliases.stream().anyMatch(a -> a.contains(normalizedQuery))) {
                    rank = 6;
                    matchedAlias = aliases.stream().filter(a -> a.contains(normalizedQuery)).findFirst().orElse(null);
                } else if (scientificName.contains(normalizedQuery) || foodCode.contains(normalizedQuery) || brandName.contains(normalizedQuery)) {
                    rank = 6;
                } else {
                    // No match
                    continue;
                }
            }

            // 3. Apply state filter
            if (state != null && !state.trim().isEmpty() && !state.equalsIgnoreCase("ALL")) {
                if (item.getFoodState() == null || !item.getFoodState().name().equalsIgnoreCase(state.trim())) {
                    continue;
                }
            }

            // 4. Apply food group / category filter
            if (group != null && !group.trim().isEmpty() && !group.equalsIgnoreCase("ALL")) {
                if (item.getFoodGroup() == null || !item.getFoodGroup().equalsIgnoreCase(group.trim())) {
                    continue;
                }
            }

            // 5. Apply data-quality status filter
            FoodNutrient nut100g = item.getNutrients().stream()
                    .filter(n -> n.getBasis() == NutrientBasis.PER_100G)
                    .findFirst()
                    .orElse(null);

            if (quality != null && !quality.trim().isEmpty() && !quality.equalsIgnoreCase("ALL")) {
                if (nut100g == null || nut100g.getQualityStatus() == null ||
                        !nut100g.getQualityStatus().name().equalsIgnoreCase(quality.trim())) {
                    continue;
                }
            }

            scoredFoods.add(new ScoredFood(item, rank, matchedAlias, nut100g));
        }

        // 6. Apply deterministic sorting
        Comparator<ScoredFood> comparator;

        String sortMode = (sort != null) ? sort.trim().toUpperCase() : (hasQuery ? "RELEVANCE" : "NAME_ASC");

        switch (sortMode) {
            case "NAME_ASC":
                comparator = Comparator.comparing((ScoredFood sf) -> sf.item.getName(), String.CASE_INSENSITIVE_ORDER);
                break;
            case "NAME_DESC":
                comparator = Comparator.comparing((ScoredFood sf) -> sf.item.getName(), String.CASE_INSENSITIVE_ORDER).reversed();
                break;
            case "CALORIES_DESC":
                comparator = Comparator.comparing((ScoredFood sf) -> sf.nut100g != null && sf.nut100g.getEnergyKcal() != null ? sf.nut100g.getEnergyKcal() : BigDecimal.ZERO, Comparator.reverseOrder())
                        .thenComparing(sf -> sf.item.getName(), String.CASE_INSENSITIVE_ORDER);
                break;
            case "CALORIES_ASC":
                comparator = Comparator.comparing((ScoredFood sf) -> sf.nut100g != null && sf.nut100g.getEnergyKcal() != null ? sf.nut100g.getEnergyKcal() : BigDecimal.ZERO)
                        .thenComparing(sf -> sf.item.getName(), String.CASE_INSENSITIVE_ORDER);
                break;
            case "RELEVANCE":
            default:
                comparator = Comparator.comparingInt((ScoredFood sf) -> sf.rank)
                        .thenComparing(sf -> sf.item.getName(), String.CASE_INSENSITIVE_ORDER);
                break;
        }

        scoredFoods.sort(comparator);

        // 7. Map to DTO with aliases and matched alias indicator
        return scoredFoods.stream()
                .map(this::mapToSummaryDto)
                .collect(Collectors.toList());
    }

    private FoodSummaryDto mapToSummaryDto(ScoredFood sf) {
        FoodItem item = sf.item;
        FoodNutrient nut100g = sf.nut100g;

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

        // Aliases list and matched alias
        if (item.getAliases() != null && !item.getAliases().isEmpty()) {
            dto.setAliases(item.getAliases().stream()
                    .map(FoodAlias::getAliasName)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()));
        }
        dto.setMatchedAlias(sf.matchedAlias);

        return dto;
    }

    private static class ScoredFood {
        final FoodItem item;
        final int rank;
        final String matchedAlias;
        final FoodNutrient nut100g;

        ScoredFood(FoodItem item, int rank, String matchedAlias, FoodNutrient nut100g) {
            this.item = item;
            this.rank = rank;
            this.matchedAlias = matchedAlias;
            this.nut100g = nut100g;
        }
    }
}
