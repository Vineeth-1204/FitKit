package com.fitkit.service;

import com.fitkit.domain.entity.FoodNutrient;
import com.fitkit.domain.enums.NutrientBasis;
import com.fitkit.domain.enums.QualityStatus;
import com.fitkit.dto.RawFoodRecordDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class NormalizationService {

    private static final Logger log = LoggerFactory.getLogger(NormalizationService.class);
    public static final BigDecimal KJ_TO_KCAL_FACTOR = new BigDecimal("4.184");
    public static final BigDecimal FAT_ATWATER_KCAL = new BigDecimal("9.0");
    public static final BigDecimal PROTEIN_ATWATER_KCAL = new BigDecimal("4.0");
    public static final BigDecimal CARB_ATWATER_KCAL = new BigDecimal("4.0");
    public static final BigDecimal FIBER_ATWATER_KCAL = new BigDecimal("2.0");

    /**
     * Converts Kilojoules to Kilocalories using standard conversion factor: kcal = kJ / 4.184
     * Preserves 4 decimal places during internal division, rounded to 2 decimal places for entity storage.
     */
    public BigDecimal convertKjToKcal(BigDecimal energyKj) {
        if (energyKj == null) return null;
        if (energyKj.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        return energyKj.divide(KJ_TO_KCAL_FACTOR, 4, RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Converts Kilocalories to Kilojoules using standard factor: kJ = kcal * 4.184
     */
    public BigDecimal convertKcalToKj(BigDecimal energyKcal) {
        if (energyKcal == null) return null;
        return energyKcal.multiply(KJ_TO_KCAL_FACTOR).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Normalizes and validates incoming nutrient data from raw sources.
     * Enforces missing-vs-zero semantics and resolves known anomalies such as the 0 kJ oil bug.
     */
    public FoodNutrient normalizeNutrients(RawFoodRecordDto raw) {
        FoodNutrient nutrient = new FoodNutrient();
        nutrient.setBasis(NutrientBasis.PER_100G);
        nutrient.setRawSourceEnergy(raw.getRawEnergyValue());

        BigDecimal protein = sanitizeDecimal(raw.getProteinG());
        BigDecimal carbs = sanitizeDecimal(raw.getAvailableCarbsG());
        BigDecimal fat = sanitizeDecimal(raw.getFatG());
        BigDecimal fiber = sanitizeDecimal(raw.getFiberG());
        BigDecimal moisture = sanitizeDecimal(raw.getMoistureG());
        BigDecimal ash = sanitizeDecimal(raw.getAshG());

        nutrient.setProteinG(protein);
        nutrient.setAvailableCarbohydratesG(carbs);
        nutrient.setTotalFatG(fat);
        nutrient.setDietaryFiberG(fiber);
        nutrient.setMoistureWaterG(moisture);
        nutrient.setAshG(ash);
        nutrient.setMicronutrientsJson(raw.getMicronutrientsJson());

        BigDecimal energyKcal = sanitizeDecimal(raw.getEnergyKcal());
        BigDecimal energyKj = sanitizeDecimal(raw.getEnergyKj());

        // Parse from raw string if explicit units are present (e.g. "1480 kJ")
        if (raw.getRawEnergyValue() != null) {
            String rawStr = raw.getRawEnergyValue().trim().toLowerCase();
            if (rawStr.endsWith("kj")) {
                try {
                    String numStr = rawStr.replace("kj", "").trim();
                    BigDecimal parsedKj = new BigDecimal(numStr);
                    energyKj = parsedKj;
                    energyKcal = convertKjToKcal(parsedKj);
                } catch (Exception ignored) {}
            } else if (rawStr.endsWith("kcal")) {
                try {
                    String numStr = rawStr.replace("kcal", "").trim();
                    BigDecimal parsedKcal = new BigDecimal(numStr);
                    energyKcal = parsedKcal;
                    energyKj = convertKcalToKj(parsedKcal);
                } catch (Exception ignored) {}
            }
        }

        // Handle case where only kJ is provided
        if (energyKcal == null && energyKj != null) {
            energyKcal = convertKjToKcal(energyKj);
        } else if (energyKj == null && energyKcal != null) {
            energyKj = convertKcalToKj(energyKcal);
        }

        // ==============================================================================
        // AUDIT STEP 3: Robust Energy Anomaly Detection & Correction
        // Authoritative rationale: In IFCT 2017, oils/fats report 100g lipids, but energy
        // was left unpopulated in the PDF table, leading community parsers to coerce null to 0.
        // We do NOT blanket convert all zeros to 900 kcal.
        // Requirements:
        // 1. Missing energy (null) MUST NOT be silently converted into genuine zero.
        // 2. High fat content alone cannot trigger an unjustified energy correction.
        // 3. Appropriate evidence required:
        //    - Either foodGroup is edible oils/fats, or code matches oil (IFCT T series),
        //      OR proximate composition is pure/nearly pure lipid (fat >= 95g, protein <= 1g, carbs <= 1g).
        //    - AND reported energy was 0 (or null in pure oil table).
        // 4. Missing energy for mixed/non-oil foods must remain null with UNVERIFIED status.
        // 5. Legitimate zero-energy foods (water, diet tea with near-zero fat/protein/carbs) remain 0.0 with SOURCE_VERIFIED.
        // ==============================================================================
        boolean isEdibleOilOrPureFat = isEdibleOilOrPureFat(raw, fat, protein, carbs);
        boolean isZeroEnergy = (energyKcal != null && energyKcal.compareTo(BigDecimal.ZERO) == 0) ||
                               (energyKj != null && energyKj.compareTo(BigDecimal.ZERO) == 0) ||
                               isExplicitZeroRawString(raw.getRawEnergyValue());
        boolean isMissingEnergy = (energyKcal == null && energyKj == null);

        if (isEdibleOilOrPureFat && (isZeroEnergy || isMissingEnergy) && fat != null && fat.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal correctedKcal = fat.multiply(FAT_ATWATER_KCAL).setScale(2, RoundingMode.HALF_UP);
            BigDecimal correctedKj = convertKcalToKj(correctedKcal);

            nutrient.setEnergyKcal(correctedKcal);
            nutrient.setEnergyKj(correctedKj);
            nutrient.setQualityStatus(QualityStatus.CORRECTED_ANOMALY);
            nutrient.setCorrectionNotes("CORRECTION AUDIT: Verified oil/fat record [" +
                    (raw.getFoodCode() != null ? raw.getFoodCode() : "N/A") +
                    "] reported zero or missing energy (" + raw.getRawEnergyValue() +
                    ") despite " + fat + "g lipid content. Energy corrected via 9.0 kcal/g Atwater standard to " +
                    correctedKcal + " kcal (" + correctedKj + " kJ).");
            log.warn("Applied energy anomaly correction for food [{}]: fat={}g -> {} kcal", raw.getName(), fat, correctedKcal);
        } else if (isMissingEnergy) {
            nutrient.setEnergyKcal(null);
            nutrient.setEnergyKj(null);
            nutrient.setQualityStatus(QualityStatus.UNVERIFIED);
            nutrient.setCorrectionNotes("DATA QUALITY NOTICE: Energy value missing from source; preserved as unmeasured (null).");
        } else {
            nutrient.setEnergyKcal(energyKcal);
            nutrient.setEnergyKj(energyKj);
            nutrient.setQualityStatus(QualityStatus.SOURCE_VERIFIED);
        }

        return nutrient;
    }

    private boolean isExplicitZeroRawString(String rawStr) {
        if (rawStr == null) return false;
        String trimmed = rawStr.trim().toLowerCase();
        return trimmed.equals("0") || trimmed.equals("0 kj") || trimmed.equals("0 kcal") || trimmed.equals("0.0") || trimmed.equals("0.00");
    }

    private boolean isEdibleOilOrPureFat(RawFoodRecordDto raw, BigDecimal fat, BigDecimal protein, BigDecimal carbs) {
        if (raw == null) return false;

        // Check food group classification
        if (raw.getFoodGroup() != null) {
            String group = raw.getFoodGroup().trim().toLowerCase();
            if (group.equals("edible oils and fats") || group.equals("fats and oils") || group.equals("oils and fats")) {
                return true;
            }
        }

        // Check authoritative source code (IFCT T-series: T001 to T014)
        if (raw.getFoodCode() != null && raw.getFoodCode().toUpperCase().contains("IFCT_T")) {
            return true;
        }
        if (raw.getSourceRecordId() != null && raw.getSourceRecordId().toUpperCase().matches("T\\d+")) {
            return true;
        }

        // Check chemical composition: pure or near-pure lipid (fat >= 95g, protein <= 1g, carbs <= 1g)
        boolean hasVeryHighFat = fat != null && fat.compareTo(new BigDecimal("95.00")) >= 0;
        boolean hasNegligibleProtein = protein == null || protein.compareTo(new BigDecimal("1.00")) <= 0;
        boolean hasNegligibleCarbs = carbs == null || carbs.compareTo(new BigDecimal("1.00")) <= 0;

        return hasVeryHighFat && hasNegligibleProtein && hasNegligibleCarbs;
    }

    /**
     * Validates physical plausibility of proximates (sum of proximate components <= 105g / 100g)
     */
    public boolean validateProximateSum(FoodNutrient n) {
        BigDecimal sum = BigDecimal.ZERO;
        if (n.getProteinG() != null) sum = sum.add(n.getProteinG());
        if (n.getAvailableCarbohydratesG() != null) sum = sum.add(n.getAvailableCarbohydratesG());
        if (n.getTotalFatG() != null) sum = sum.add(n.getTotalFatG());
        if (n.getDietaryFiberG() != null) sum = sum.add(n.getDietaryFiberG());
        if (n.getMoistureWaterG() != null) sum = sum.add(n.getMoistureWaterG());
        if (n.getAshG() != null) sum = sum.add(n.getAshG());

        // Allow up to 105.0g to account for standard analytical variance (+-5%)
        return sum.compareTo(new BigDecimal("105.0")) <= 0;
    }

    private BigDecimal sanitizeDecimal(BigDecimal val) {
        if (val == null) return null;
        return val.setScale(2, RoundingMode.HALF_UP);
    }
}
