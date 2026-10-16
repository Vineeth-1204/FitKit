package com.fitkit;

import com.fitkit.domain.entity.FoodItem;
import com.fitkit.domain.entity.FoodNutrient;
import com.fitkit.domain.enums.FoodState;
import com.fitkit.domain.enums.QualityStatus;
import com.fitkit.dto.RawFoodRecordDto;
import com.fitkit.dto.ValidationReportDto;
import com.fitkit.repository.FoodItemRepository;
import com.fitkit.repository.FoodNutrientRepository;
import com.fitkit.service.DataImportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DataImportPipelineTest {

    @Autowired
    private DataImportService importService;

    @Autowired
    private FoodItemRepository foodRepo;

    @Autowired
    private FoodNutrientRepository nutrientRepo;

    @Test
    @DisplayName("Should import records idempotently without duplicating existing foods")
    void testIdempotentImport() {
        RawFoodRecordDto rawPoha = new RawFoodRecordDto();
        rawPoha.setSourceId("IFCT_2017");
        rawPoha.setSourceRecordId("A011");
        rawPoha.setFoodCode("IFCT_A011_POHA");
        rawPoha.setName("Rice flakes (Poha / Aval)");
        rawPoha.setFoodGroup("Cereals and Millets");
        rawPoha.setFoodState(FoodState.RAW_INGREDIENT);
        rawPoha.setRawEnergyValue("1480 kJ");
        rawPoha.setProteinG(new BigDecimal("7.44"));
        rawPoha.setAvailableCarbsG(new BigDecimal("76.75"));
        rawPoha.setFatG(new BigDecimal("1.14"));
        rawPoha.setFiberG(new BigDecimal("3.46"));
        rawPoha.setMoistureG(new BigDecimal("10.36"));

        // First Import
        ValidationReportDto report1 = importService.importBatch("IFCT_2017", List.of(rawPoha));
        assertThat(report1.getImportedCount()).isEqualTo(1);
        assertThat(report1.getSkippedDuplicates()).isEqualTo(0);

        Optional<FoodItem> saved1 = foodRepo.findByFoodCode("IFCT_A011_POHA");
        assertThat(saved1).isPresent();
        assertThat(saved1.get().getName()).isEqualTo("Rice flakes (Poha / Aval)");

        // Second Import (identical batch) - Must be skipped without error
        ValidationReportDto report2 = importService.importBatch("IFCT_2017", List.of(rawPoha));
        assertThat(report2.getImportedCount()).isEqualTo(0);
        assertThat(report2.getSkippedDuplicates()).isEqualTo(1); // Idempotent skip
    }

    @Test
    @DisplayName("Should detect and correct 0 kJ cooking oil bug during pipeline ingestion")
    void testCookingOilCorrectionDuringImport() {
        RawFoodRecordDto rawMustard = new RawFoodRecordDto();
        rawMustard.setSourceId("IFCT_2017");
        rawMustard.setSourceRecordId("T006");
        rawMustard.setFoodCode("IFCT_T006_MUSTARD");
        rawMustard.setName("Mustard oil");
        rawMustard.setFoodGroup("Edible Oils and Fats");
        rawMustard.setFoodState(FoodState.RAW_INGREDIENT);
        rawMustard.setRawEnergyValue("0 kJ"); // Digitized anomaly
        rawMustard.setProteinG(BigDecimal.ZERO);
        rawMustard.setAvailableCarbsG(BigDecimal.ZERO);
        rawMustard.setFatG(new BigDecimal("100.00"));
        rawMustard.setFiberG(BigDecimal.ZERO);
        rawMustard.setMoistureG(BigDecimal.ZERO);

        ValidationReportDto report = importService.importBatch("IFCT_2017", List.of(rawMustard));

        assertThat(report.getImportedCount()).isEqualTo(1);
        assertThat(report.getCorrectedCount()).isEqualTo(1);
        assertThat(report.getAnomalyAuditLogs()).isNotEmpty();
        assertThat(report.getAnomalyAuditLogs().get(0)).contains("CORRECTION AUDIT");

        FoodItem saved = foodRepo.findByFoodCode("IFCT_T006_MUSTARD").orElseThrow();
        FoodNutrient nut = nutrientRepo.findByFoodIdAndBasis(saved.getId(), com.fitkit.domain.enums.NutrientBasis.PER_100G).orElseThrow();

        assertThat(nut.getEnergyKcal()).isEqualByComparingTo("900.00");
        assertThat(nut.getEnergyKj()).isEqualByComparingTo("3765.60");
        assertThat(nut.getQualityStatus()).isEqualTo(QualityStatus.CORRECTED_ANOMALY);
    }

    @Test
    @DisplayName("Should reject invalid records with missing food code or excessive proximate sum")
    void testRejectInvalidRecords() {
        RawFoodRecordDto invalidCode = new RawFoodRecordDto();
        invalidCode.setFoodCode(""); // Invalid
        invalidCode.setName("Missing Code Item");

        RawFoodRecordDto excessiveProximate = new RawFoodRecordDto();
        excessiveProximate.setFoodCode("INVALID_SUM_01");
        excessiveProximate.setName("Impossible Food");
        excessiveProximate.setProteinG(new BigDecimal("60.0"));
        excessiveProximate.setAvailableCarbsG(new BigDecimal("60.0")); // Sum = 120g > 105g

        ValidationReportDto report = importService.importBatch("IFCT_2017", List.of(invalidCode, excessiveProximate));

        assertThat(report.getImportedCount()).isEqualTo(0);
        assertThat(report.getRejectedCount()).isEqualTo(2);
        assertThat(report.getRejectionReasons()).hasSize(2);
    }

    @Test
    @DisplayName("Should preserve provenance data accurately")
    void testProvenancePreservation() {
        RawFoodRecordDto raw = new RawFoodRecordDto();
        raw.setSourceId("USDA_FDC");
        raw.setSourceRecordId("173424");
        raw.setFoodCode("USDA_173424_BOILED_EGG");
        raw.setName("Egg, whole, cooked, hard-boiled");
        raw.setFoodGroup("Dairy and Egg Products");
        raw.setFoodState(FoodState.COOKED_SINGLE);
        raw.setRawEnergyValue("155 kcal");
        raw.setProteinG(new BigDecimal("12.58"));
        raw.setAvailableCarbsG(new BigDecimal("1.12"));
        raw.setFatG(new BigDecimal("10.61"));
        raw.setFiberG(BigDecimal.ZERO);
        raw.setMoistureG(new BigDecimal("74.62"));

        importService.importBatch("USDA_FDC", List.of(raw));

        FoodItem saved = foodRepo.findByFoodCode("USDA_173424_BOILED_EGG").orElseThrow();
        assertThat(saved.getSource().getId()).isEqualTo("USDA_FDC");
        assertThat(saved.getSourceRecordId()).isEqualTo("173424");
        assertThat(saved.getFoodState()).isEqualTo(FoodState.COOKED_SINGLE);

        FoodNutrient nut = nutrientRepo.findByFoodIdAndBasis(saved.getId(), com.fitkit.domain.enums.NutrientBasis.PER_100G).orElseThrow();
        assertThat(nut.getRawSourceEnergy()).isEqualTo("155 kcal");
        assertThat(nut.getEnergyKcal()).isEqualByComparingTo("155.00");
    }
}
