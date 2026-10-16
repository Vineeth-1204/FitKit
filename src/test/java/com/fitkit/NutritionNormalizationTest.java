package com.fitkit;

import com.fitkit.domain.entity.FoodNutrient;
import com.fitkit.domain.enums.QualityStatus;
import com.fitkit.dto.RawFoodRecordDto;
import com.fitkit.service.NormalizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class NutritionNormalizationTest {

    private NormalizationService service;

    @BeforeEach
    void setUp() {
        service = new NormalizationService();
    }

    @Test
    @DisplayName("Should convert kJ to kcal accurately (1480 kJ -> 353.73 kcal)")
    void testConvertKjToKcal() {
        BigDecimal kj = new BigDecimal("1480");
        BigDecimal kcal = service.convertKjToKcal(kj);

        // 1480 / 4.184 = 353.7285... -> 353.73
        assertThat(kcal).isNotNull();
        assertThat(kcal).isEqualByComparingTo("353.73");
    }

    @Test
    @DisplayName("Should convert kcal to kJ accurately (365 kcal -> 1527.16 kJ)")
    void testConvertKcalToKj() {
        BigDecimal kcal = new BigDecimal("365.0");
        BigDecimal kj = service.convertKcalToKj(kcal);

        // 365.0 * 4.184 = 1527.16
        assertThat(kj).isNotNull();
        assertThat(kj).isEqualByComparingTo("1527.16");
    }

    @Test
    @DisplayName("Should preserve missing nutrient values as null and genuine zeros as 0.0")
    void testMissingVsZeroSemantics() {
        RawFoodRecordDto raw = new RawFoodRecordDto();
        raw.setFoodCode("TEST_01");
        raw.setName("Test Ingredient");
        raw.setProteinG(new BigDecimal("12.50"));
        raw.setFatG(BigDecimal.ZERO); // Genuine zero fat
        raw.setDietaryFiberG(null);  // Unmeasured / missing fiber
        raw.setEnergyKcal(new BigDecimal("50.0"));

        FoodNutrient nutrient = service.normalizeNutrients(raw);

        assertThat(nutrient.getProteinG()).isEqualByComparingTo("12.50");
        assertThat(nutrient.getTotalFatG()).isEqualByComparingTo("0.00");
        assertThat(nutrient.getDietaryFiberG()).isNull(); // NEVER coerced to 0.0
    }

    @Test
    @DisplayName("Should detect and correct 0 kJ pure cooking oil anomaly via Atwater lipid factor")
    void testPureOilZeroKjCorrection() {
        RawFoodRecordDto oilRaw = new RawFoodRecordDto();
        oilRaw.setFoodCode("IFCT_T006");
        oilRaw.setName("Mustard oil");
        oilRaw.setFoodGroup("Edible Oils and Fats");
        oilRaw.setFatG(new BigDecimal("100.0"));
        oilRaw.setProteinG(BigDecimal.ZERO);
        oilRaw.setAvailableCarbsG(BigDecimal.ZERO);
        oilRaw.setDietaryFiberG(BigDecimal.ZERO);
        oilRaw.setRawEnergyValue("0 kJ"); // The digitized bug

        FoodNutrient nutrient = service.normalizeNutrients(oilRaw);

        // Corrected to 100 * 9.0 = 900.0 kcal / 3765.6 kJ
        assertThat(nutrient.getEnergyKcal()).isEqualByComparingTo("900.00");
        assertThat(nutrient.getEnergyKj()).isEqualByComparingTo("3765.60");
        assertThat(nutrient.getQualityStatus()).isEqualTo(QualityStatus.CORRECTED_ANOMALY);
        assertThat(nutrient.getCorrectionNotes()).contains("CORRECTION AUDIT");
        assertThat(nutrient.getRawSourceEnergy()).isEqualTo("0 kJ");
    }

    @Test
    @DisplayName("Should detect and correct missing/zero energy for Ghee (IFCT_T013)")
    void testGheeEnergyAnomalyCorrection() {
        RawFoodRecordDto gheeRaw = new RawFoodRecordDto();
        gheeRaw.setSourceRecordId("T013");
        gheeRaw.setFoodCode("IFCT_T013");
        gheeRaw.setName("Ghee (Clarified Butter)");
        gheeRaw.setFoodGroup("Edible Oils and Fats");
        gheeRaw.setFatG(new BigDecimal("100.00"));
        gheeRaw.setProteinG(BigDecimal.ZERO);
        gheeRaw.setAvailableCarbsG(BigDecimal.ZERO);
        gheeRaw.setRawEnergyValue(null); // Missing energy in table
        gheeRaw.setEnergyKcal(null);

        FoodNutrient nutrient = service.normalizeNutrients(gheeRaw);

        assertThat(nutrient.getEnergyKcal()).isEqualByComparingTo("900.00");
        assertThat(nutrient.getEnergyKj()).isEqualByComparingTo("3765.60");
        assertThat(nutrient.getQualityStatus()).isEqualTo(QualityStatus.CORRECTED_ANOMALY);
        assertThat(nutrient.getCorrectionNotes()).contains("CORRECTION AUDIT");
    }

    @Test
    @DisplayName("Should preserve legitimate zero-energy foods (e.g. water or green tea) without correction")
    void testLegitimateZeroEnergyFood() {
        RawFoodRecordDto waterRaw = new RawFoodRecordDto();
        waterRaw.setFoodCode("WATER_01");
        waterRaw.setName("Drinking Water");
        waterRaw.setFatG(BigDecimal.ZERO);
        waterRaw.setProteinG(BigDecimal.ZERO);
        waterRaw.setAvailableCarbsG(BigDecimal.ZERO);
        waterRaw.setDietaryFiberG(BigDecimal.ZERO);
        waterRaw.setRawEnergyValue("0 kcal");

        FoodNutrient nutrient = service.normalizeNutrients(waterRaw);

        assertThat(nutrient.getEnergyKcal()).isEqualByComparingTo("0.00");
        assertThat(nutrient.getEnergyKj()).isEqualByComparingTo("0.00");
        assertThat(nutrient.getQualityStatus()).isEqualTo(QualityStatus.SOURCE_VERIFIED);
        assertThat(nutrient.getCorrectionNotes()).isNull();
    }

    @Test
    @DisplayName("Should NOT trigger unjustified energy correction for high-fat food with missing energy")
    void testHighFatRecordWithMissingEnergy() {
        RawFoodRecordDto avocadoRaw = new RawFoodRecordDto();
        avocadoRaw.setFoodCode("AVOCADO_RAW_01");
        avocadoRaw.setName("Fresh Avocado");
        avocadoRaw.setFoodGroup("Fruits");
        avocadoRaw.setFatG(new BigDecimal("15.40")); // High fat content
        avocadoRaw.setProteinG(new BigDecimal("2.00"));
        avocadoRaw.setAvailableCarbsG(new BigDecimal("8.50"));
        avocadoRaw.setDietaryFiberG(new BigDecimal("6.70"));
        avocadoRaw.setEnergyKcal(null); // Missing energy
        avocadoRaw.setEnergyKj(null);
        avocadoRaw.setRawEnergyValue(null);

        FoodNutrient nutrient = service.normalizeNutrients(avocadoRaw);

        // MUST NOT be silently converted to 0.0, and MUST NOT be converted to 15.4 * 9 = 138.6 kcal
        assertThat(nutrient.getEnergyKcal()).isNull();
        assertThat(nutrient.getEnergyKj()).isNull();
        assertThat(nutrient.getQualityStatus()).isEqualTo(QualityStatus.UNVERIFIED);
        assertThat(nutrient.getCorrectionNotes()).contains("DATA QUALITY NOTICE: Energy value missing");
    }

    @Test
    @DisplayName("Should validate proximate sum bounds (<= 105g is valid, > 105g is rejected)")
    void testValidateProximateSum() {
        FoodNutrient valid = new FoodNutrient();
        valid.setProteinG(new BigDecimal("10.0"));
        valid.setAvailableCarbohydratesG(new BigDecimal("70.0"));
        valid.setTotalFatG(new BigDecimal("1.0"));
        valid.setMoistureWaterG(new BigDecimal("12.0"));
        valid.setDietaryFiberG(new BigDecimal("2.0"));
        valid.setAshG(new BigDecimal("1.0"));
        // Sum = 96.0g
        assertThat(service.validateProximateSum(valid)).isTrue();

        FoodNutrient invalid = new FoodNutrient();
        invalid.setProteinG(new BigDecimal("50.0"));
        invalid.setAvailableCarbohydratesG(new BigDecimal("60.0")); // Sum = 110.0g > 105g
        assertThat(service.validateProximateSum(invalid)).isFalse();
    }
}
