package com.fitkit;

import com.fitkit.domain.entity.FoodItem;
import com.fitkit.domain.entity.FoodNutrient;
import com.fitkit.domain.entity.FoodPortion;
import com.fitkit.domain.enums.FoodState;
import com.fitkit.domain.enums.NutrientBasis;
import com.fitkit.domain.enums.QualityStatus;
import com.fitkit.dto.FoodSummaryDto;
import com.fitkit.repository.FoodItemRepository;
import com.fitkit.service.FoodSearchService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class FoodSearchAndCatalogExpansionTest {

    @Autowired
    private FoodSearchService foodSearchService;

    @Autowired
    private FoodItemRepository foodItemRepository;

    @Nested
    @DisplayName("1. Phase 7 Catalog Expansion & Nutrition Integrity")
    class CatalogExpansionTests {

        @Test
        @DisplayName("Catalog contains all 18 Phase 7 staple ingredients (Total >= 51 foods)")
        void testPhase7StapleFoodsExist() {
            long totalCount = foodItemRepository.count();
            assertTrue(totalCount >= 51, "Catalog should have at least 51 items after Phase 7 expansion, found: " + totalCount);

            // Verify a selection of staples across groups
            assertFoodExists("IFCT_A008", "Brown Rice, raw grain", "Oryza sativa", "Cereal Grains and Pastas");
            assertFoodExists("IFCT_A015", "Rolled Oats / Oatmeal, raw flakes", "Avena sativa", "Cereal Grains and Pastas");
            assertFoodExists("IFCT_B006", "Besan / Bengal Gram Flour", "Cicer arietinum", "Grain Legumes and Pulses");
            assertFoodExists("IFCT_G031", "Tamarind, raw pulp (Imli)", "Tamarindus indica", "Condiments and Spices");
            assertFoodExists("IFCT_A011", "Raw Poha / Flattened Rice Flakes", "Oryza sativa", "Cereal Grains and Pastas");
        }

        @Test
        @DisplayName("Nutrient values and source provenance are correctly recorded for new staples")
        void testNutrientAndProvenanceAccuracy() {
            // Verify Besan (IFCT B006)
            FoodItem besan = foodItemRepository.findByFoodCode("IFCT_B006").orElseThrow();
            assertEquals("IFCT_2017", besan.getSource().getId());
            assertEquals("B006", besan.getSourceRecordId());

            FoodNutrient besanNut = besan.getNutrients().stream()
                    .filter(n -> n.getBasis() == NutrientBasis.PER_100G)
                    .findFirst()
                    .orElseThrow();
            assertEquals(0, new BigDecimal("365.70").compareTo(besanNut.getEnergyKcal()));
            assertEquals(0, new BigDecimal("1530.00").compareTo(besanNut.getEnergyKj()));
            assertEquals(0, new BigDecimal("21.80").compareTo(besanNut.getProteinG()));
            assertEquals(0, new BigDecimal("56.60").compareTo(besanNut.getAvailableCarbohydratesG()));
            assertEquals(0, new BigDecimal("5.30").compareTo(besanNut.getTotalFatG()));
            assertEquals(0, new BigDecimal("12.80").compareTo(besanNut.getDietaryFiberG()));
            assertEquals(QualityStatus.SOURCE_VERIFIED, besanNut.getQualityStatus());

            // Verify Portions exist
            assertFalse(besan.getPortions().isEmpty(), "Besan must have portion definitions");
            boolean hasDefaultPortion = besan.getPortions().stream().anyMatch(FoodPortion::getDefaultPortion);
            assertTrue(hasDefaultPortion, "Besan must have a default portion defined");
        }

        @Test
        @DisplayName("Preserve Phase 6.1 reconciled items (Mustard Oil anomaly, Cumin, Turmeric, Onion, Semolina)")
        void testPhase6ReconciledItemsPreserved() {
            // Mustard Oil should be CORRECTED_ANOMALY (Food 33, IFCT_T017)
            FoodItem mustardOil = foodItemRepository.findByFoodCode("IFCT_T017").orElseThrow();
            FoodNutrient moNut = mustardOil.getNutrients().stream()
                    .filter(n -> n.getBasis() == NutrientBasis.PER_100G)
                    .findFirst()
                    .orElseThrow();
            assertEquals(QualityStatus.CORRECTED_ANOMALY, moNut.getQualityStatus(), "Mustard oil must preserve CORRECTED_ANOMALY");
            assertEquals(0, new BigDecimal("884.00").compareTo(moNut.getEnergyKcal()), "Mustard oil must strictly preserve 884.00 kcal");
            assertEquals(0, new BigDecimal("3700.00").compareTo(moNut.getEnergyKj()), "Mustard oil must strictly preserve 3700.00 kJ");

            // Semolina should have IFCT_A022 with 333.70 kcal (from Phase 6.1 reconciliation)
            FoodItem semolina = foodItemRepository.findByFoodCode("IFCT_A022").orElseThrow();
            FoodNutrient semNut = semolina.getNutrients().stream()
                    .filter(n -> n.getBasis() == NutrientBasis.PER_100G)
                    .findFirst()
                    .orElseThrow();
            assertEquals(0, new BigDecimal("333.70").compareTo(semNut.getEnergyKcal()));
        }

        @Test
        @DisplayName("Mustard Oil calorie integrity & CALORIES_DESC sorting (Ghee 900 > Mustard Oil 884 > Butter 714)")
        void testMustardOilCalorieSortingIntegrity() {
            List<FoodSummaryDto> calDesc = foodSearchService.searchAndFilterFoods(null, null, null, null, "CALORIES_DESC");
            assertTrue(calDesc.size() >= 3);

            FoodSummaryDto first = calDesc.get(0);
            FoodSummaryDto second = calDesc.get(1);
            FoodSummaryDto third = calDesc.get(2);

            assertEquals("Ghee (Clarified Butter)", first.getName());
            assertEquals(0, new BigDecimal("900.00").compareTo(first.getEnergyKcal()));

            assertEquals("Mustard Oil, cold-pressed", second.getName());
            assertEquals(0, new BigDecimal("884.00").compareTo(second.getEnergyKcal()));
            assertEquals("CORRECTED_ANOMALY", second.getQualityStatus());

            assertEquals("Amul Pasteurized Butter", third.getName());
            assertEquals(0, new BigDecimal("714.30").compareTo(third.getEnergyKcal()));
        }

        @Test
        @DisplayName("Reconciled food groups and populated analytical micronutrients (V9)")
        void testReconciledFoodGroupsAndMicronutrients() {
            FoodItem curryLeaves = foodItemRepository.findByFoodCode("IFCT_C009").orElseThrow();
            assertEquals("Green Leafy Vegetables", curryLeaves.getFoodGroup());

            FoodItem coriander = foodItemRepository.findByFoodCode("IFCT_C008").orElseThrow();
            assertEquals("Green Leafy Vegetables", coriander.getFoodGroup());

            FoodItem spinach = foodItemRepository.findByFoodCode("IFCT_C019").orElseThrow();
            assertEquals("Green Leafy Vegetables", spinach.getFoodGroup());

            FoodItem potato = foodItemRepository.findByFoodCode("IFCT_E010").orElseThrow();
            assertEquals("Roots and Tubers", potato.getFoodGroup());

            // Micronutrients check
            FoodNutrient spinachNut = spinach.getNutrients().get(0);
            assertNotNull(spinachNut.getMicronutrientsJson(), "Spinach must have micronutrients populated");
            assertTrue(spinachNut.getMicronutrientsJson().contains("calcium_mg"));
            assertTrue(spinachNut.getMicronutrientsJson().contains("iron_mg"));
            assertTrue(spinachNut.getMicronutrientsJson().contains("vitamin_c_mg"));

            // DTO check
            List<FoodSummaryDto> results = foodSearchService.searchAndFilterFoods("spinach", null, null, null, "RELEVANCE");
            assertFalse(results.isEmpty());
            assertNotNull(results.get(0).getMicronutrientsJson());
        }

        private void assertFoodExists(String foodCode, String expectedName, String expectedScientificName, String expectedGroup) {
            Optional<FoodItem> opt = foodItemRepository.findByFoodCode(foodCode);
            assertTrue(opt.isPresent(), "Food with code " + foodCode + " should be present in catalog");
            FoodItem f = opt.get();
            assertEquals(expectedName, f.getName());
            assertEquals(expectedScientificName, f.getScientificName());
            assertEquals(expectedGroup, f.getFoodGroup());
            assertEquals(FoodState.RAW_INGREDIENT, f.getFoodState());
        }
    }

    @Nested
    @DisplayName("2. Food Search Aliases and Ranking")
    class SearchAliasesAndRankingTests {

        @Test
        @DisplayName("Exact alias match returns intended food at high rank (suji -> Semolina)")
        void testExactAliasMatchSuji() {
            List<FoodSummaryDto> results = foodSearchService.searchAndFilterFoods("suji", null, null, null, "RELEVANCE");
            assertFalse(results.isEmpty(), "Searching 'suji' should return results");
            assertEquals("Semolina / Suji / Rava", results.get(0).getName(), "Top result for 'suji' must be Semolina");
            assertEquals("suji", results.get(0).getMatchedAlias());
        }

        @Test
        @DisplayName("Alternate alias matches same food (rava -> Semolina)")
        void testAlternateAliasMatchRava() {
            List<FoodSummaryDto> results = foodSearchService.searchAndFilterFoods("rava", null, null, null, "RELEVANCE");
            assertFalse(results.isEmpty());
            assertEquals("Semolina / Suji / Rava", results.get(0).getName());
            assertEquals("rava", results.get(0).getMatchedAlias());
        }

        @Test
        @DisplayName("Search atta returns Whole Wheat Flour")
        void testAliasMatchAtta() {
            List<FoodSummaryDto> results = foodSearchService.searchAndFilterFoods("atta", null, null, null, "RELEVANCE");
            assertFalse(results.isEmpty());
            assertEquals("Wheat flour, whole (Atta)", results.get(0).getName());
        }

        @Test
        @DisplayName("Search dahi returns Curd / Dahi")
        void testAliasMatchDahi() {
            List<FoodSummaryDto> results = foodSearchService.searchAndFilterFoods("dahi", null, null, null, "RELEVANCE");
            assertFalse(results.isEmpty());
            assertTrue(results.get(0).getName().contains("Curd") || results.get(0).getName().contains("Dahi"));
        }

        @Test
        @DisplayName("Search pyaaz / kanda returns Onion")
        void testAliasMatchPyaaz() {
            List<FoodSummaryDto> results = foodSearchService.searchAndFilterFoods("pyaaz", null, null, null, "RELEVANCE");
            assertFalse(results.isEmpty());
            assertTrue(results.get(0).getName().contains("Onion"));
        }

        @Test
        @DisplayName("Search tamatar returns Tomato")
        void testAliasMatchTamatar() {
            List<FoodSummaryDto> results = foodSearchService.searchAndFilterFoods("tamatar", null, null, null, "RELEVANCE");
            assertFalse(results.isEmpty());
            assertTrue(results.get(0).getName().contains("Tomato"));
        }

        @Test
        @DisplayName("Search aloo returns Potato")
        void testAliasMatchAloo() {
            List<FoodSummaryDto> results = foodSearchService.searchAndFilterFoods("aloo", null, null, null, "RELEVANCE");
            assertFalse(results.isEmpty());
            assertTrue(results.get(0).getName().contains("Potato") || results.get(0).getName().contains("Aloo"));
        }

        @Test
        @DisplayName("Search adrak returns Ginger")
        void testAliasMatchAdrak() {
            List<FoodSummaryDto> results = foodSearchService.searchAndFilterFoods("adrak", null, null, null, "RELEVANCE");
            assertFalse(results.isEmpty());
            assertTrue(results.get(0).getName().contains("Ginger") || results.get(0).getName().contains("Adrak"));
        }

        @Test
        @DisplayName("Search lahsun returns Garlic")
        void testAliasMatchLahsun() {
            List<FoodSummaryDto> results = foodSearchService.searchAndFilterFoods("lahsun", null, null, null, "RELEVANCE");
            assertFalse(results.isEmpty());
            assertTrue(results.get(0).getName().contains("Garlic") || results.get(0).getName().contains("Lahsun"));
        }

        @Test
        @DisplayName("Deterministic ranking prefers exact canonical match over alias and partial matches")
        void testRankingHierarchy() {
            // "Ginger" canonical name starts with or equals
            List<FoodSummaryDto> results = foodSearchService.searchAndFilterFoods("ginger", null, null, null, "RELEVANCE");
            assertFalse(results.isEmpty());
            assertEquals("Ginger, fresh raw (Adrak)", results.get(0).getName());

            // "Garlic"
            List<FoodSummaryDto> garlicResults = foodSearchService.searchAndFilterFoods("garlic", null, null, null, "RELEVANCE");
            assertFalse(garlicResults.isEmpty());
            assertEquals("Garlic, fresh raw (Lahsun)", garlicResults.get(0).getName());
        }

        @Test
        @DisplayName("Multiple aliases matching the same food do not produce duplicate entries")
        void testNoDuplicateResultsForMultiAliasMatches() {
            // "rice" matches Brown rice, White rice, Idli rice, Poha, etc.
            List<FoodSummaryDto> results = foodSearchService.searchAndFilterFoods("rice", null, null, null, "RELEVANCE");
            long distinctIds = results.stream().map(FoodSummaryDto::getId).distinct().count();
            assertEquals(results.size(), distinctIds, "Search results must be deduplicated by food ID");
        }

        @Test
        @DisplayName("Empty or whitespace-only search returns all items without error")
        void testEmptyAndWhitespaceSearch() {
            List<FoodSummaryDto> emptyResults = foodSearchService.searchAndFilterFoods("", null, null, null, null);
            List<FoodSummaryDto> wsResults = foodSearchService.searchAndFilterFoods("   ", null, null, null, null);
            List<FoodSummaryDto> nullResults = foodSearchService.searchAndFilterFoods(null, null, null, null, null);

            assertFalse(emptyResults.isEmpty());
            assertEquals(emptyResults.size(), wsResults.size());
            assertEquals(emptyResults.size(), nullResults.size());
        }

        @Test
        @DisplayName("Non-matching search returns clean empty list")
        void testNonMatchingSearch() {
            List<FoodSummaryDto> results = foodSearchService.searchAndFilterFoods("xyznonexistentingredient123", null, null, null, null);
            assertNotNull(results);
            assertTrue(results.isEmpty(), "Non-existent food should yield empty result list");
        }
    }

    @Nested
    @DisplayName("3. Practical Food Library Filters")
    class LibraryFilterTests {

        @Test
        @DisplayName("Filter by food state: RAW_INGREDIENT vs COOKED_SINGLE")
        void testFoodStateFilter() {
            List<FoodSummaryDto> rawFoods = foodSearchService.searchAndFilterFoods(null, "RAW_INGREDIENT", null, null, null);
            assertFalse(rawFoods.isEmpty());
            for (FoodSummaryDto f : rawFoods) {
                assertEquals("RAW_INGREDIENT", f.getFoodState());
            }

            List<FoodSummaryDto> cookedFoods = foodSearchService.searchAndFilterFoods(null, "COOKED_SINGLE", null, null, null);
            assertFalse(cookedFoods.isEmpty());
            for (FoodSummaryDto f : cookedFoods) {
                assertEquals("COOKED_SINGLE", f.getFoodState());
            }
        }

        @Test
        @DisplayName("Filter by category / food group")
        void testFoodGroupFilter() {
            List<FoodSummaryDto> pulses = foodSearchService.searchAndFilterFoods(null, null, "Grain Legumes and Pulses", null, null);
            assertFalse(pulses.isEmpty());
            for (FoodSummaryDto f : pulses) {
                assertEquals("Grain Legumes and Pulses", f.getFoodGroup());
            }
        }

        @Test
        @DisplayName("Filter by data quality status (SOURCE_VERIFIED vs CORRECTED_ANOMALY)")
        void testQualityStatusFilter() {
            List<FoodSummaryDto> verified = foodSearchService.searchAndFilterFoods(null, null, null, "SOURCE_VERIFIED", null);
            assertFalse(verified.isEmpty());
            for (FoodSummaryDto f : verified) {
                assertEquals("SOURCE_VERIFIED", f.getQualityStatus());
            }

            List<FoodSummaryDto> corrected = foodSearchService.searchAndFilterFoods(null, null, null, "CORRECTED_ANOMALY", null);
            assertFalse(corrected.isEmpty());
            for (FoodSummaryDto f : corrected) {
                assertEquals("CORRECTED_ANOMALY", f.getQualityStatus());
            }
        }

        @Test
        @DisplayName("Combined search query and multiple filters work simultaneously")
        void testCombinedSearchAndFilters() {
            // Search "dal" with group "Grain Legumes and Pulses" and state "RAW_INGREDIENT"
            List<FoodSummaryDto> results = foodSearchService.searchAndFilterFoods("dal", "RAW_INGREDIENT", "Grain Legumes and Pulses", "SOURCE_VERIFIED", "RELEVANCE");
            assertFalse(results.isEmpty());
            for (FoodSummaryDto f : results) {
                assertEquals("RAW_INGREDIENT", f.getFoodState());
                assertEquals("Grain Legumes and Pulses", f.getFoodGroup());
                assertEquals("SOURCE_VERIFIED", f.getQualityStatus());
            }
        }

        @Test
        @DisplayName("Sorting modes: NAME_ASC, NAME_DESC, CALORIES_DESC, CALORIES_ASC")
        void testSortingModes() {
            List<FoodSummaryDto> nameAsc = foodSearchService.searchAndFilterFoods(null, null, null, null, "NAME_ASC");
            for (int i = 0; i < nameAsc.size() - 1; i++) {
                assertTrue(nameAsc.get(i).getName().compareToIgnoreCase(nameAsc.get(i + 1).getName()) <= 0);
            }

            List<FoodSummaryDto> calDesc = foodSearchService.searchAndFilterFoods(null, null, null, null, "CALORIES_DESC");
            for (int i = 0; i < calDesc.size() - 1; i++) {
                BigDecimal c1 = calDesc.get(i).getEnergyKcal() != null ? calDesc.get(i).getEnergyKcal() : BigDecimal.ZERO;
                BigDecimal c2 = calDesc.get(i + 1).getEnergyKcal() != null ? calDesc.get(i + 1).getEnergyKcal() : BigDecimal.ZERO;
                assertTrue(c1.compareTo(c2) >= 0, "Calorie DESC order violated: " + c1 + " vs " + c2);
            }
        }
    }
}
