package com.fitkit.service;

import com.fitkit.domain.entity.*;
import com.fitkit.domain.enums.MealType;
import com.fitkit.domain.enums.NutrientBasis;
import com.fitkit.dto.*;
import com.fitkit.repository.FoodItemRepository;
import com.fitkit.repository.MealEntryRepository;
import com.fitkit.repository.MealLogRepository;
import com.fitkit.repository.RecipeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;

@Service
public class MealLoggingService {

    private final MealLogRepository mealLogRepository;
    private final MealEntryRepository mealEntryRepository;
    private final FoodItemRepository foodItemRepository;
    private final RecipeRepository recipeRepository;
    private final RecipeCalculationService recipeCalculationService;
    private final UserTargetService userTargetService;

    public MealLoggingService(MealLogRepository mealLogRepository,
                              MealEntryRepository mealEntryRepository,
                              FoodItemRepository foodItemRepository,
                              RecipeRepository recipeRepository,
                              RecipeCalculationService recipeCalculationService,
                              UserTargetService userTargetService) {
        this.mealLogRepository = mealLogRepository;
        this.mealEntryRepository = mealEntryRepository;
        this.foodItemRepository = foodItemRepository;
        this.recipeRepository = recipeRepository;
        this.recipeCalculationService = recipeCalculationService;
        this.userTargetService = userTargetService;
    }

    @Transactional(readOnly = true)
    public DailyMealLogDto getDailyMealLog(String userId, LocalDate date) {
        String uid = sanitizeUserId(userId);
        LocalDate targetDate = (date != null) ? date : LocalDate.now();

        Optional<MealLog> mealLogOpt = mealLogRepository.findByUserIdAndLogDateWithEntries(uid, targetDate);
        UserTargetDto targets = userTargetService.getTargets(uid);

        return buildDailyMealLogDto(mealLogOpt.orElse(null), uid, targetDate, targets);
    }

    @Transactional
    public DailyMealLogDto addMealEntry(String userId, CreateMealEntryRequestDto request) {
        String uid = sanitizeUserId(userId);
        LocalDate logDate = (request.getLogDate() != null) ? request.getLogDate() : LocalDate.now();
        MealType mealType = MealType.fromString(request.getMealType());

        MealLog mealLog = mealLogRepository.findByUserIdAndLogDate(uid, logDate)
                .orElseGet(() -> {
                    MealLog ml = new MealLog(uid, logDate);
                    return mealLogRepository.save(ml);
                });

        MealEntry entry = new MealEntry();
        entry.setMealLog(mealLog);
        entry.setMealType(mealType);

        BigDecimal qty = (request.getQuantityMultiplier() != null && request.getQuantityMultiplier().compareTo(BigDecimal.ZERO) > 0)
                ? request.getQuantityMultiplier()
                : BigDecimal.ONE;
        entry.setQuantityMultiplier(qty);

        BigDecimal portionWeight = (request.getPortionGramWeight() != null && request.getPortionGramWeight().compareTo(BigDecimal.ZERO) > 0)
                ? request.getPortionGramWeight()
                : new BigDecimal("100.00");
        entry.setPortionGramWeight(portionWeight);

        BigDecimal totalWeight = portionWeight.multiply(qty).setScale(2, RoundingMode.HALF_UP);
        entry.setTotalGramWeight(totalWeight);

        String portionDesc = (request.getPortionDescription() != null && !request.getPortionDescription().trim().isEmpty())
                ? request.getPortionDescription().trim()
                : portionWeight.setScale(0, RoundingMode.HALF_UP) + "g";
        entry.setPortionDescription(portionDesc);

        // Resolve Food or Recipe and compute authoritative nutrition snapshot
        if (request.getFoodId() != null) {
            FoodItem food = foodItemRepository.findByIdWithDetails(request.getFoodId())
                    .orElseThrow(() -> new IllegalArgumentException("Food item not found with ID: " + request.getFoodId()));

            entry.setFood(food);
            entry.setFoodName(food.getName());
            entry.setFoodState(food.getFoodState().name());

            computeFoodNutrientSnapshot(entry, food, totalWeight);
        } else if (request.getRecipeId() != null) {
            Recipe recipe = recipeRepository.findByIdWithIngredients(request.getRecipeId())
                    .orElseThrow(() -> new IllegalArgumentException("Recipe not found with ID: " + request.getRecipeId()));

            // Authorization: If custom recipe, ensure caller owns it
            if (recipe.getIsCustom() != null && recipe.getIsCustom()) {
                if (recipe.getUserId() == null || !recipe.getUserId().equalsIgnoreCase(uid)) {
                    throw new SecurityException("Access denied: You cannot log another user's private recipe.");
                }
            }

            entry.setRecipe(recipe);
            entry.setFoodName(recipe.getTitle());
            entry.setFoodState("PREPARED_RECIPE");

            computeRecipeNutrientSnapshot(entry, recipe, portionWeight, qty, totalWeight);
        } else {
            throw new IllegalArgumentException("Either foodId or recipeId must be specified.");
        }

        mealLog.addEntry(entry);
        mealLogRepository.save(mealLog);

        return getDailyMealLog(uid, logDate);
    }

    @Transactional
    public DailyMealLogDto updateMealEntry(String userId, Long entryId, UpdateMealEntryRequestDto request) {
        String uid = sanitizeUserId(userId);
        MealEntry entry = mealEntryRepository.findById(entryId)
                .orElseThrow(() -> new IllegalArgumentException("Meal entry not found with ID: " + entryId));

        if (!entry.getMealLog().getUserId().equalsIgnoreCase(uid)) {
            throw new SecurityException("Access denied: Entry belongs to another user.");
        }

        if (request.getQuantityMultiplier() != null && request.getQuantityMultiplier().compareTo(BigDecimal.ZERO) > 0) {
            entry.setQuantityMultiplier(request.getQuantityMultiplier());
        }

        if (request.getPortionGramWeight() != null && request.getPortionGramWeight().compareTo(BigDecimal.ZERO) > 0) {
            entry.setPortionGramWeight(request.getPortionGramWeight());
        }

        if (request.getPortionDescription() != null && !request.getPortionDescription().trim().isEmpty()) {
            entry.setPortionDescription(request.getPortionDescription().trim());
        }

        BigDecimal totalWeight = entry.getPortionGramWeight().multiply(entry.getQuantityMultiplier()).setScale(2, RoundingMode.HALF_UP);
        entry.setTotalGramWeight(totalWeight);

        // Recompute snapshot
        if (entry.getFood() != null) {
            computeFoodNutrientSnapshot(entry, entry.getFood(), totalWeight);
        } else if (entry.getRecipe() != null) {
            computeRecipeNutrientSnapshot(entry, entry.getRecipe(), entry.getPortionGramWeight(), entry.getQuantityMultiplier(), totalWeight);
        } else {
            // Proportional scale from existing snapshot if referenced food was deleted
            BigDecimal prevWeight = (entry.getTotalGramWeight() != null && entry.getTotalGramWeight().compareTo(BigDecimal.ZERO) > 0)
                    ? entry.getTotalGramWeight() : BigDecimal.valueOf(100);
            BigDecimal scale = totalWeight.divide(prevWeight, 4, RoundingMode.HALF_UP);
            entry.setEnergyKcal(entry.getEnergyKcal().multiply(scale).setScale(2, RoundingMode.HALF_UP));
            if (entry.getEnergyKj() != null) entry.setEnergyKj(entry.getEnergyKj().multiply(scale).setScale(2, RoundingMode.HALF_UP));
            entry.setProteinG(entry.getProteinG().multiply(scale).setScale(2, RoundingMode.HALF_UP));
            entry.setCarbohydratesG(entry.getCarbohydratesG().multiply(scale).setScale(2, RoundingMode.HALF_UP));
            entry.setFatG(entry.getFatG().multiply(scale).setScale(2, RoundingMode.HALF_UP));
            entry.setFiberG(entry.getFiberG().multiply(scale).setScale(2, RoundingMode.HALF_UP));
        }

        mealEntryRepository.save(entry);
        return getDailyMealLog(uid, entry.getMealLog().getLogDate());
    }

    @Transactional
    public DailyMealLogDto deleteMealEntry(String userId, Long entryId) {
        String uid = sanitizeUserId(userId);
        MealEntry entry = mealEntryRepository.findById(entryId)
                .orElseThrow(() -> new IllegalArgumentException("Meal entry not found with ID: " + entryId));

        MealLog log = entry.getMealLog();
        if (!log.getUserId().equalsIgnoreCase(uid)) {
            throw new SecurityException("Access denied: Entry belongs to another user.");
        }

        LocalDate logDate = log.getLogDate();
        log.removeEntry(entry);
        mealLogRepository.save(log);

        return getDailyMealLog(uid, logDate);
    }

    @Transactional(readOnly = true)
    public List<DailyHistoryItemDto> getWeeklyHistory(String userId, LocalDate endDate, int days) {
        String uid = sanitizeUserId(userId);
        LocalDate end = (endDate != null) ? endDate : LocalDate.now();
        int dayCount = (days > 0 && days <= 31) ? days : 7;
        LocalDate start = end.minusDays(dayCount - 1);

        List<MealLog> logs = mealLogRepository.findHistoryWithEntries(uid, start, end);
        Map<LocalDate, MealLog> logMap = new HashMap<>();
        for (MealLog l : logs) {
            logMap.put(l.getLogDate(), l);
        }

        List<DailyHistoryItemDto> result = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (int i = 0; i < dayCount; i++) {
            LocalDate curDate = start.plusDays(i);
            String dayLabel = curDate.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            DailyHistoryItemDto item = new DailyHistoryItemDto(curDate, curDate.format(fmt), dayLabel);

            MealLog curLog = logMap.get(curDate);
            if (curLog != null && curLog.getEntries() != null && !curLog.getEntries().isEmpty()) {
                BigDecimal cal = BigDecimal.ZERO;
                BigDecimal p = BigDecimal.ZERO;
                BigDecimal c = BigDecimal.ZERO;
                BigDecimal f = BigDecimal.ZERO;
                BigDecimal fib = BigDecimal.ZERO;

                for (MealEntry e : curLog.getEntries()) {
                    cal = cal.add(e.getEnergyKcal() != null ? e.getEnergyKcal() : BigDecimal.ZERO);
                    p = p.add(e.getProteinG() != null ? e.getProteinG() : BigDecimal.ZERO);
                    c = c.add(e.getCarbohydratesG() != null ? e.getCarbohydratesG() : BigDecimal.ZERO);
                    f = f.add(e.getFatG() != null ? e.getFatG() : BigDecimal.ZERO);
                    fib = fib.add(e.getFiberG() != null ? e.getFiberG() : BigDecimal.ZERO);
                }

                item.setTotalCalories(cal.setScale(0, RoundingMode.HALF_UP));
                item.setTotalProteinG(p.setScale(1, RoundingMode.HALF_UP));
                item.setTotalCarbsG(c.setScale(1, RoundingMode.HALF_UP));
                item.setTotalFatG(f.setScale(1, RoundingMode.HALF_UP));
                item.setTotalFiberG(fib.setScale(1, RoundingMode.HALF_UP));
                item.setHasEntries(true);
            }

            result.add(item);
        }

        return result;
    }

    @Transactional
    public int migrateLocalEntries(String userId, List<ClientDiaryMigrationDto> migrationList) {
        String uid = sanitizeUserId(userId);
        if (migrationList == null || migrationList.isEmpty()) {
            return 0;
        }

        int importedCount = 0;

        for (ClientDiaryMigrationDto diaryDto : migrationList) {
            LocalDate logDate = diaryDto.getDate();
            if (logDate == null) continue;

            MealLog mealLog = mealLogRepository.findByUserIdAndLogDateWithEntries(uid, logDate)
                    .orElseGet(() -> {
                        MealLog ml = new MealLog(uid, logDate);
                        return mealLogRepository.save(ml);
                    });

            // If log already has entries in DB, skip migration for this date to avoid duplicate double-import
            if (mealLog.getEntries() != null && !mealLog.getEntries().isEmpty()) {
                continue;
            }

            Map<String, List<ClientDiaryMigrationDto.MigratedItem>> meals = diaryDto.getMeals();
            if (meals == null) continue;

            for (Map.Entry<String, List<ClientDiaryMigrationDto.MigratedItem>> entryGroup : meals.entrySet()) {
                MealType mealType = MealType.fromString(entryGroup.getKey());
                List<ClientDiaryMigrationDto.MigratedItem> items = entryGroup.getValue();
                if (items == null) continue;

                for (ClientDiaryMigrationDto.MigratedItem it : items) {
                    MealEntry me = new MealEntry();
                    me.setMealLog(mealLog);
                    me.setMealType(mealType);
                    me.setFoodName(it.getName() != null ? it.getName() : "Imported Food");
                    me.setFoodState(it.getState() != null ? it.getState() : "COOKED_SINGLE");
                    me.setPortionDescription(it.getPortionDesc() != null ? it.getPortionDesc() : "1 serving");

                    BigDecimal gw = it.getGramWeight() != null ? it.getGramWeight() : BigDecimal.valueOf(100);
                    me.setPortionGramWeight(gw);
                    me.setQuantityMultiplier(BigDecimal.ONE);
                    me.setTotalGramWeight(gw);

                    me.setEnergyKcal(it.getCalories() != null ? it.getCalories() : BigDecimal.ZERO);
                    me.setProteinG(it.getProtein() != null ? it.getProtein() : BigDecimal.ZERO);
                    me.setCarbohydratesG(it.getCarbs() != null ? it.getCarbs() : BigDecimal.ZERO);
                    me.setFatG(it.getFat() != null ? it.getFat() : BigDecimal.ZERO);
                    me.setFiberG(it.getFiber() != null ? it.getFiber() : BigDecimal.ZERO);

                    if (it.getFoodId() != null) {
                        foodItemRepository.findById(it.getFoodId()).ifPresent(me::setFood);
                    }

                    mealLog.addEntry(me);
                    importedCount++;
                }
            }

            mealLogRepository.save(mealLog);
        }

        return importedCount;
    }

    private void computeFoodNutrientSnapshot(MealEntry entry, FoodItem food, BigDecimal totalGramWeight) {
        BigDecimal ratio = totalGramWeight.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);

        FoodNutrient nut = (food.getNutrients() != null && !food.getNutrients().isEmpty())
                ? food.getNutrients().stream()
                .filter(n -> n.getBasis() == NutrientBasis.PER_100G)
                .findFirst()
                .orElse(food.getNutrients().get(0))
                : null;

        if (nut != null) {
            BigDecimal kcal = nut.getEnergyKcal() != null ? nut.getEnergyKcal().multiply(ratio) : BigDecimal.ZERO;
            BigDecimal kj = nut.getEnergyKj() != null ? nut.getEnergyKj().multiply(ratio) : null;
            BigDecimal p = nut.getProteinG() != null ? nut.getProteinG().multiply(ratio) : BigDecimal.ZERO;
            BigDecimal c = nut.getAvailableCarbohydratesG() != null ? nut.getAvailableCarbohydratesG().multiply(ratio) : BigDecimal.ZERO;
            BigDecimal f = nut.getTotalFatG() != null ? nut.getTotalFatG().multiply(ratio) : BigDecimal.ZERO;
            BigDecimal fib = nut.getDietaryFiberG() != null ? nut.getDietaryFiberG().multiply(ratio) : BigDecimal.ZERO;

            entry.setEnergyKcal(kcal.setScale(2, RoundingMode.HALF_UP));
            if (kj != null) entry.setEnergyKj(kj.setScale(2, RoundingMode.HALF_UP));
            entry.setProteinG(p.setScale(2, RoundingMode.HALF_UP));
            entry.setCarbohydratesG(c.setScale(2, RoundingMode.HALF_UP));
            entry.setFatG(f.setScale(2, RoundingMode.HALF_UP));
            entry.setFiberG(fib.setScale(2, RoundingMode.HALF_UP));
        } else {
            entry.setEnergyKcal(BigDecimal.ZERO);
            entry.setProteinG(BigDecimal.ZERO);
            entry.setCarbohydratesG(BigDecimal.ZERO);
            entry.setFatG(BigDecimal.ZERO);
            entry.setFiberG(BigDecimal.ZERO);
        }
    }

    private void computeRecipeNutrientSnapshot(MealEntry entry, Recipe recipe, BigDecimal portionGramWeight, BigDecimal qty, BigDecimal totalGramWeight) {
        RecipeCalculationResultDto calc = recipeCalculationService.calculateRecipe(recipe);

        BigDecimal ratio = totalGramWeight.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
        BigDecimal kcal = calc.getEnergyKcalPer100g().multiply(ratio);
        BigDecimal p = calc.getProteinGPer100g().multiply(ratio);
        BigDecimal c = calc.getAvailableCarbsGPer100g().multiply(ratio);
        BigDecimal f = calc.getFatGPer100g().multiply(ratio);
        BigDecimal fib = calc.getFiberGPer100g() != null ? calc.getFiberGPer100g().multiply(ratio) : BigDecimal.ZERO;

        entry.setEnergyKcal(kcal.setScale(2, RoundingMode.HALF_UP));
        entry.setProteinG(p.setScale(2, RoundingMode.HALF_UP));
        entry.setCarbohydratesG(c.setScale(2, RoundingMode.HALF_UP));
        entry.setFatG(f.setScale(2, RoundingMode.HALF_UP));
        entry.setFiberG(fib.setScale(2, RoundingMode.HALF_UP));
    }

    private DailyMealLogDto buildDailyMealLogDto(MealLog log, String userId, LocalDate date, UserTargetDto targets) {
        DailyMealLogDto dto = new DailyMealLogDto();
        dto.setUserId(userId);
        dto.setDate(date);
        dto.setId(log != null ? log.getId() : null);
        dto.setNotes(log != null ? log.getNotes() : null);

        // Targets
        dto.setTargetCalories(targets.getDailyCalories());
        dto.setTargetProteinG(targets.getProteinG());
        dto.setTargetCarbsG(targets.getCarbsG());
        dto.setTargetFatG(targets.getFatG());
        dto.setTargetFiberG(targets.getFiberG());

        // Initialize sections
        Map<String, MealSectionDto> sections = new LinkedHashMap<>();
        for (MealType mt : MealType.values()) {
            sections.put(mt.getDisplayName(), new MealSectionDto(mt.getDisplayName()));
        }

        BigDecimal totalCal = BigDecimal.ZERO;
        BigDecimal totalP = BigDecimal.ZERO;
        BigDecimal totalC = BigDecimal.ZERO;
        BigDecimal totalF = BigDecimal.ZERO;
        BigDecimal totalFib = BigDecimal.ZERO;

        if (log != null && log.getEntries() != null) {
            for (MealEntry e : log.getEntries()) {
                String sectionKey = e.getMealType().getDisplayName();
                MealSectionDto sec = sections.get(sectionKey);
                if (sec == null) {
                    sec = new MealSectionDto(sectionKey);
                    sections.put(sectionKey, sec);
                }

                MealEntryDto ed = mapToEntryDto(e);
                sec.getEntries().add(ed);

                sec.setSubtotalCalories(sec.getSubtotalCalories().add(e.getEnergyKcal()));
                sec.setSubtotalProteinG(sec.getSubtotalProteinG().add(e.getProteinG()));
                sec.setSubtotalCarbsG(sec.getSubtotalCarbsG().add(e.getCarbohydratesG()));
                sec.setSubtotalFatG(sec.getSubtotalFatG().add(e.getFatG()));
                sec.setSubtotalFiberG(sec.getSubtotalFiberG().add(e.getFiberG()));

                totalCal = totalCal.add(e.getEnergyKcal());
                totalP = totalP.add(e.getProteinG());
                totalC = totalC.add(e.getCarbohydratesG());
                totalF = totalF.add(e.getFatG());
                totalFib = totalFib.add(e.getFiberG());
            }
        }

        dto.setMeals(sections);
        dto.setTotalCalories(totalCal.setScale(0, RoundingMode.HALF_UP));
        dto.setTotalProteinG(totalP.setScale(1, RoundingMode.HALF_UP));
        dto.setTotalCarbsG(totalC.setScale(1, RoundingMode.HALF_UP));
        dto.setTotalFatG(totalF.setScale(1, RoundingMode.HALF_UP));
        dto.setTotalFiberG(totalFib.setScale(1, RoundingMode.HALF_UP));

        if (targets.getDailyCalories() != null) {
            BigDecimal targetCalBD = BigDecimal.valueOf(targets.getDailyCalories());
            dto.setRemainingCalories(targetCalBD.subtract(totalCal).setScale(0, RoundingMode.HALF_UP));
            int pct = totalCal.multiply(BigDecimal.valueOf(100)).divide(targetCalBD, 0, RoundingMode.HALF_UP).intValue();
            dto.setCaloriePercentage(pct);
        }

        return dto;
    }

    private MealEntryDto mapToEntryDto(MealEntry e) {
        MealEntryDto dto = new MealEntryDto();
        dto.setId(e.getId());
        dto.setMealType(e.getMealType().getDisplayName());
        dto.setFoodId(e.getFood() != null ? e.getFood().getId() : null);
        dto.setRecipeId(e.getRecipe() != null ? e.getRecipe().getId() : null);
        dto.setFoodName(e.getFoodName());
        dto.setFoodState(e.getFoodState());
        dto.setPortionDescription(e.getPortionDescription());
        dto.setPortionGramWeight(e.getPortionGramWeight());
        dto.setQuantityMultiplier(e.getQuantityMultiplier());
        dto.setTotalGramWeight(e.getTotalGramWeight());
        dto.setEnergyKcal(e.getEnergyKcal());
        dto.setEnergyKj(e.getEnergyKj());
        dto.setProteinG(e.getProteinG());
        dto.setCarbohydratesG(e.getCarbohydratesG());
        dto.setFatG(e.getFatG());
        dto.setFiberG(e.getFiberG());
        return dto;
    }

    private String sanitizeUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return "default-user";
        }
        return userId.trim();
    }
}
