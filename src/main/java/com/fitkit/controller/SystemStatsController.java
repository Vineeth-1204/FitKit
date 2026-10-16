package com.fitkit.controller;

import com.fitkit.domain.entity.FoodItem;
import com.fitkit.domain.entity.ImportAuditLog;
import com.fitkit.domain.entity.SourceProvenance;
import com.fitkit.dto.SystemStatsDto;
import com.fitkit.repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/stats")
@CrossOrigin(origins = "*")
public class SystemStatsController {

    private final FoodItemRepository foodItemRepository;
    private final FoodNutrientRepository foodNutrientRepository;
    private final FoodPortionRepository foodPortionRepository;
    private final RecipeRepository recipeRepository;
    private final SourceProvenanceRepository sourceProvenanceRepository;
    private final ImportAuditLogRepository importAuditLogRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public SystemStatsController(FoodItemRepository foodItemRepository,
                                 FoodNutrientRepository foodNutrientRepository,
                                 FoodPortionRepository foodPortionRepository,
                                 RecipeRepository recipeRepository,
                                 SourceProvenanceRepository sourceProvenanceRepository,
                                 ImportAuditLogRepository importAuditLogRepository) {
        this.foodItemRepository = foodItemRepository;
        this.foodNutrientRepository = foodNutrientRepository;
        this.foodPortionRepository = foodPortionRepository;
        this.recipeRepository = recipeRepository;
        this.sourceProvenanceRepository = sourceProvenanceRepository;
        this.importAuditLogRepository = importAuditLogRepository;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<SystemStatsDto> getSystemStats() {
        SystemStatsDto dto = new SystemStatsDto();

        List<FoodItem> allFoods = foodItemRepository.findAll();
        dto.setTotalFoods(allFoods.size());
        dto.setVerifiedFoods(allFoods.stream().filter(f -> Boolean.TRUE.equals(f.getVerified())).count());
        dto.setTotalRecipes(recipeRepository.count());
        dto.setTotalNutrientProfiles(foodNutrientRepository.count());
        dto.setTotalPortions(foodPortionRepository.count());

        Map<String, Long> countBySource = allFoods.stream()
                .filter(f -> f.getSource() != null)
                .collect(Collectors.groupingBy(f -> f.getSource().getId(), Collectors.counting()));

        List<SourceProvenance> sources = sourceProvenanceRepository.findAll();
        List<SystemStatsDto.SourceSummary> sourceSummaries = new ArrayList<>();
        for (SourceProvenance sp : sources) {
            long count = countBySource.getOrDefault(sp.getId(), 0L);
            sourceSummaries.add(new SystemStatsDto.SourceSummary(
                    sp.getId(),
                    sp.getDatasetName(),
                    sp.getPublisher(),
                    sp.getLicenseType(),
                    count
            ));
        }
        dto.setSources(sourceSummaries);

        List<ImportAuditLog> logs = importAuditLogRepository.findAll();
        List<SystemStatsDto.AuditLogSummary> auditSummaries = new ArrayList<>();
        for (ImportAuditLog log : logs) {
            SystemStatsDto.AuditLogSummary as = new SystemStatsDto.AuditLogSummary();
            as.setId(log.getId());
            as.setBatchId("BATCH-" + log.getId());
            as.setSourceName(log.getSource() != null ? log.getSource().getDatasetName() : "Unknown");
            as.setTotalRecords(log.getRecordsRead() != null ? log.getRecordsRead() : 0);
            as.setSuccessCount(log.getRecordsImported() != null ? log.getRecordsImported() : 0);
            as.setSkippedDuplicates(log.getRecordsSkippedDuplicate() != null ? log.getRecordsSkippedDuplicate() : 0);
            as.setAnomalyCorrections(log.getRecordsCorrected() != null ? log.getRecordsCorrected() : 0);
            as.setStatus(log.getRecordsRejectedInvalid() != null && log.getRecordsRejectedInvalid() > 0 ? "PARTIAL" : "COMPLETED");
            if (log.getImportTimestamp() != null) {
                as.setExecutedAt(log.getImportTimestamp().format(FORMATTER));
            }
            auditSummaries.add(as);
        }
        dto.setRecentAuditLogs(auditSummaries);

        return ResponseEntity.ok(dto);
    }
}
