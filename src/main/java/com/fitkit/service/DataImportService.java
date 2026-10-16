package com.fitkit.service;

import com.fitkit.domain.entity.*;
import com.fitkit.domain.enums.QualityStatus;
import com.fitkit.dto.RawFoodRecordDto;
import com.fitkit.dto.ValidationReportDto;
import com.fitkit.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DataImportService {

    private static final Logger log = LoggerFactory.getLogger(DataImportService.class);

    private final SourceProvenanceRepository sourceRepo;
    private final FoodItemRepository foodRepo;
    private final FoodNutrientRepository nutrientRepo;
    private final FoodPortionRepository portionRepo;
    private final ImportAuditLogRepository auditRepo;
    private final NormalizationService normalizationService;

    public DataImportService(SourceProvenanceRepository sourceRepo,
                             FoodItemRepository foodRepo,
                             FoodNutrientRepository nutrientRepo,
                             FoodPortionRepository portionRepo,
                             ImportAuditLogRepository auditRepo,
                             NormalizationService normalizationService) {
        this.sourceRepo = sourceRepo;
        this.foodRepo = foodRepo;
        this.nutrientRepo = nutrientRepo;
        this.portionRepo = portionRepo;
        this.auditRepo = auditRepo;
        this.normalizationService = normalizationService;
    }

    /**
     * Idempotent import pipeline for batch ingestion.
     * Prevents duplicate imports, audits energy corrections, enforces proximate validation,
     * and produces an import audit trail.
     */
    @Transactional
    public ValidationReportDto importBatch(String sourceId, List<RawFoodRecordDto> records) {
        ValidationReportDto report = new ValidationReportDto(sourceId);

        SourceProvenance source = sourceRepo.findById(sourceId).orElse(null);
        if (source == null) {
            report.incrementRejected("Source provenance not registered: " + sourceId);
            return report;
        }

        for (RawFoodRecordDto raw : records) {
            report.incrementRead();

            // 1. Validate mandatory fields
            if (raw.getFoodCode() == null || raw.getFoodCode().trim().isEmpty()) {
                report.incrementRejected("Record missing mandatory foodCode: " + raw.getName());
                continue;
            }
            if (raw.getName() == null || raw.getName().trim().isEmpty()) {
                report.incrementRejected("Record missing mandatory name for code: " + raw.getFoodCode());
                continue;
            }

            // 2. Check for duplicate record (Idempotence guarantee)
            Optional<FoodItem> existing = foodRepo.findByFoodCode(raw.getFoodCode());
            if (existing.isPresent()) {
                report.incrementSkipped();
                log.debug("Skipping existing duplicate record with foodCode: {}", raw.getFoodCode());
                continue;
            }

            // 3. Normalize nutrients and handle anomalies
            FoodNutrient nutrient = normalizationService.normalizeNutrients(raw);

            // 4. Validate physical proximate plausibility
            if (!normalizationService.validateProximateSum(nutrient)) {
                report.incrementRejected("Proximate sum exceeds physical bound of 105g/100g for food: " + raw.getFoodCode());
                continue;
            }

            // 5. Audit energy correction if applicable (e.g. 0 kJ cooking oil bug)
            if (nutrient.getQualityStatus() == QualityStatus.CORRECTED_ANOMALY) {
                report.incrementCorrected("[" + raw.getFoodCode() + " - " + raw.getName() + "]: " + nutrient.getCorrectionNotes());
            }

            // 6. Persist FoodItem entity
            FoodItem food = new FoodItem();
            food.setFoodCode(raw.getFoodCode());
            food.setSource(source);
            food.setSourceRecordId(raw.getSourceRecordId());
            food.setName(raw.getName());
            food.setScientificName(raw.getScientificName());
            food.setFoodGroup(raw.getFoodGroup());
            food.setFoodState(raw.getFoodState());
            food.setBrandName(raw.getBrandName());
            food.setBarcode(raw.getBarcode());
            food.setVerified(true);
            food.setActive(true);

            FoodItem savedFood = foodRepo.save(food);

            // 7. Persist FoodNutrient entity
            nutrient.setFood(savedFood);
            nutrientRepo.save(nutrient);

            // 8. Persist FoodPortion entities
            if (raw.getPortions() != null && !raw.getPortions().isEmpty()) {
                for (RawFoodRecordDto.PortionDto pDto : raw.getPortions()) {
                    if (pDto.getGramWeight() != null && pDto.getGramWeight().signum() > 0) {
                        FoodPortion portion = new FoodPortion(
                                savedFood,
                                pDto.getDescription() != null ? pDto.getDescription() : "Standard serving",
                                pDto.getGramWeight(),
                                pDto.getDefault() != null && pDto.getDefault(),
                                "Imported with baseline food record"
                        );
                        portionRepo.save(portion);
                    }
                }
            }

            report.incrementImported();
        }

        // 9. Persist audit log record
        ImportAuditLog auditLog = new ImportAuditLog();
        auditLog.setSource(source);
        auditLog.setImportTimestamp(LocalDateTime.now());
        auditLog.setRecordsRead(report.getTotalRead());
        auditLog.setRecordsImported(report.getImportedCount());
        auditLog.setRecordsSkippedDuplicate(report.getSkippedDuplicates());
        auditLog.setRecordsCorrected(report.getCorrectedCount());
        auditLog.setRecordsRejectedInvalid(report.getRejectedCount());
        auditLog.setLogSummary(report.toString());
        auditRepo.save(auditLog);

        return report;
    }
}
