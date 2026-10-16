package com.fitkit.service;

import com.fitkit.domain.entity.NutritionProfile;
import com.fitkit.dto.NutritionProfileDto;
import com.fitkit.dto.UserTargetDto;
import com.fitkit.repository.NutritionProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class NutritionProfileService {
    private final NutritionProfileRepository profileRepository;
    private final UserTargetService targetService;

    public NutritionProfileService(NutritionProfileRepository profileRepository, UserTargetService targetService) {
        this.profileRepository = profileRepository;
        this.targetService = targetService;
    }

    @Transactional(readOnly = true)
    public NutritionProfileDto getProfile(String userId) {
        return profileRepository.findByUserId(normalize(userId))
                .map(this::toDto)
                .orElseGet(NutritionProfileDto::new);
    }

    @Transactional
    public NutritionProfileDto saveProfile(String userId, NutritionProfileDto request) {
        validate(request);
        String uid = normalize(userId);
        NutritionProfile profile = profileRepository.findByUserId(uid).orElseGet(NutritionProfile::new);
        profile.setUserId(uid);
        profile.setAge(request.getAge());
        profile.setSexForEstimate(request.getSexForEstimate());
        profile.setHeightCm(request.getHeightCm());
        profile.setWeightKg(request.getWeightKg());
        profile.setActivityLevel(request.getActivityLevel());
        profile.setGoal(request.getGoal());
        profileRepository.save(profile);

        int calories = calculateCalories(request);
        double protein = roundOne(request.getWeightKg() * 0.8);
        double fat = roundOne(calories * 0.30 / 9.0);
        double carbs = roundOne(Math.max(0, (calories - protein * 4 - fat * 9) / 4.0));
        double fiber = roundOne(calories * 14.0 / 1000.0);
        targetService.saveTargets(uid, new UserTargetDto(uid, calories, bd(protein), bd(carbs), bd(fat), bd(fiber)));

        return toDto(profile, calories, protein, carbs, fat, fiber);
    }

    static int calculateCalories(NutritionProfileDto profile) {
        double sexConstant = switch (profile.getSexForEstimate()) {
            case "FEMALE" -> -161.0;
            case "MALE" -> 5.0;
            default -> -78.0;
        };
        double bmr = 10 * profile.getWeightKg() + 6.25 * profile.getHeightCm() - 5 * profile.getAge() + sexConstant;
        double activity = switch (profile.getActivityLevel()) {
            case "LIGHT" -> 1.375;
            case "MODERATE" -> 1.55;
            case "HIGH" -> 1.725;
            default -> 1.2;
        };
        double tdee = bmr * activity;
        double estimate = switch (profile.getGoal()) {
            case "LOSE" -> Math.max(bmr, tdee * 0.85);
            case "GAIN" -> tdee * 1.10;
            default -> tdee;
        };
        int rounded = (int) (Math.round(estimate / 50.0) * 50);
        return Math.max(1200, Math.min(5000, rounded));
    }

    private void validate(NutritionProfileDto p) {
        if (p.getAge() == null || p.getAge() < 18 || p.getAge() > 100
                || p.getHeightCm() == null || p.getHeightCm() < 120 || p.getHeightCm() > 230
                || p.getWeightKg() == null || !Double.isFinite(p.getWeightKg()) || p.getWeightKg() < 35 || p.getWeightKg() > 300
                || !oneOf(p.getSexForEstimate(), "FEMALE", "MALE", "UNSPECIFIED")
                || !oneOf(p.getActivityLevel(), "SEDENTARY", "LIGHT", "MODERATE", "HIGH")
                || !oneOf(p.getGoal(), "LOSE", "MAINTAIN", "GAIN")) {
            throw new IllegalArgumentException("Complete each field with a supported adult profile value.");
        }
    }

    private boolean oneOf(String value, String... options) {
        if (value == null) return false;
        for (String option : options) if (option.equals(value)) return true;
        return false;
    }

    private NutritionProfileDto toDto(NutritionProfile p) {
        UserTargetDto t = targetService.getTargets(p.getUserId());
        Integer calories = (t != null) ? t.getDailyCalories() : null;
        Double protein = (t != null && t.getProteinG() != null) ? t.getProteinG().doubleValue() : null;
        Double carbs = (t != null && t.getCarbsG() != null) ? t.getCarbsG().doubleValue() : null;
        Double fat = (t != null && t.getFatG() != null) ? t.getFatG().doubleValue() : null;
        Double fiber = (t != null && t.getFiberG() != null) ? t.getFiberG().doubleValue() : null;

        if (calories == null) {
            NutritionProfileDto temp = new NutritionProfileDto();
            temp.setAge(p.getAge());
            temp.setSexForEstimate(p.getSexForEstimate());
            temp.setHeightCm(p.getHeightCm());
            temp.setWeightKg(p.getWeightKg());
            temp.setActivityLevel(p.getActivityLevel());
            temp.setGoal(p.getGoal());
            calories = calculateCalories(temp);
            protein = roundOne(p.getWeightKg() * 0.8);
            fat = roundOne(calories * 0.30 / 9.0);
            carbs = roundOne(Math.max(0, (calories - protein * 4.0 - fat * 9.0) / 4.0));
            fiber = roundOne(calories * 14.0 / 1000.0);
        }
        return toDto(p, calories, protein, carbs, fat, fiber);
    }

    private NutritionProfileDto toDto(NutritionProfile p, Integer calories, Double protein, Double carbs, Double fat, Double fiber) {
        NutritionProfileDto dto = new NutritionProfileDto();
        dto.setComplete(true);
        dto.setAge(p.getAge());
        dto.setSexForEstimate(p.getSexForEstimate());
        dto.setHeightCm(p.getHeightCm());
        dto.setWeightKg(p.getWeightKg());
        dto.setActivityLevel(p.getActivityLevel());
        dto.setGoal(p.getGoal());
        dto.setDailyCalories(calories);
        dto.setProteinG(protein);
        dto.setCarbsG(carbs);
        dto.setFatG(fat);
        dto.setFiberG(fiber);
        return dto;
    }

    private String normalize(String userId) {
        if (userId == null || userId.isBlank()) throw new IllegalArgumentException("Authenticated user is required.");
        return userId.trim().toLowerCase();
    }

    private double asDouble(BigDecimal value) { return value == null ? 0 : value.doubleValue(); }
    private double roundOne(double value) { return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP).doubleValue(); }
    private BigDecimal bd(double value) { return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP); }
}
