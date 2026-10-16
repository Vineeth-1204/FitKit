package com.fitkit.controller;

import com.fitkit.dto.*;
import com.fitkit.service.MealLoggingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/meals")
public class MealLogController {

    private final MealLoggingService mealLoggingService;

    public MealLogController(MealLoggingService mealLoggingService) {
        this.mealLoggingService = mealLoggingService;
    }

    @GetMapping
    public ResponseEntity<DailyMealLogDto> getDailyMealLog(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Principal principal) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        String userId = principal.getName();
        return ResponseEntity.ok(mealLoggingService.getDailyMealLog(userId, targetDate));
    }

    @PostMapping("/entries")
    public ResponseEntity<DailyMealLogDto> addMealEntry(
            @RequestBody CreateMealEntryRequestDto request,
            Principal principal) {
        String userId = principal.getName();
        return ResponseEntity.ok(mealLoggingService.addMealEntry(userId, request));
    }

    @PutMapping("/entries/{id}")
    public ResponseEntity<DailyMealLogDto> updateMealEntry(
            @PathVariable Long id,
            @RequestBody UpdateMealEntryRequestDto request,
            Principal principal) {
        String userId = principal.getName();
        return ResponseEntity.ok(mealLoggingService.updateMealEntry(userId, id, request));
    }

    @DeleteMapping("/entries/{id}")
    public ResponseEntity<DailyMealLogDto> deleteMealEntry(
            @PathVariable Long id,
            Principal principal) {
        String userId = principal.getName();
        return ResponseEntity.ok(mealLoggingService.deleteMealEntry(userId, id));
    }

    @GetMapping("/history")
    public ResponseEntity<List<DailyHistoryItemDto>> getWeeklyHistory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "7") int days,
            Principal principal) {
        LocalDate end = (endDate != null) ? endDate : LocalDate.now();
        String userId = principal.getName();
        return ResponseEntity.ok(mealLoggingService.getWeeklyHistory(userId, end, days));
    }

    @PostMapping("/migrate")
    public ResponseEntity<Map<String, Object>> migrateLocalEntries(
            @RequestBody List<ClientDiaryMigrationDto> migrationList,
            Principal principal) {
        String userId = principal.getName();
        int count = mealLoggingService.migrateLocalEntries(userId, migrationList);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "migratedEntriesCount", count,
                "userId", userId
        ));
    }
}
