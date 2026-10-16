package com.fitkit;

import com.fitkit.dto.NutritionProfileDto;
import com.fitkit.dto.UserTargetDto;
import com.fitkit.service.NutritionProfileService;
import com.fitkit.service.UserTargetService;
import com.fitkit.service.AuthService;
import com.fitkit.dto.RegisterRequestDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class NutritionOnboardingIntegrationTest {
    @Autowired NutritionProfileService profileService;
    @Autowired UserTargetService targetService;
    @Autowired AuthService authService;

    @Test
    void newAccountHasNoGenericTargetAndReceivesTargetsFromItsAnswers() {
        String userId = "new-user@fitkit.local";
        authService.register(new RegisterRequestDto(userId, "StrongPassword1!", "New User"));
        assertNull(targetService.getTargets(userId).getDailyCalories());

        NutritionProfileDto saved = profileService.saveProfile(userId,
                profile(30, "FEMALE", 165, 65, "MODERATE", "LOSE"));

        assertTrue(saved.isComplete());
        assertTrue(saved.getDailyCalories() > 0);
        assertTrue(saved.getProteinG() > 0);
        assertEquals(saved.getDailyCalories(), profileService.getProfile(userId).getDailyCalories());
        UserTargetDto persisted = targetService.getTargets(userId);
        assertEquals(saved.getDailyCalories(), persisted.getDailyCalories());
    }

    @Test
    void differentProfilesProduceDifferentTargets() {
        authService.register(new RegisterRequestDto("a@fitkit.local", "StrongPassword1!", "A"));
        authService.register(new RegisterRequestDto("b@fitkit.local", "StrongPassword1!", "B"));
        NutritionProfileDto smallerSedentary = profileService.saveProfile("a@fitkit.local",
                profile(25, "FEMALE", 155, 50, "SEDENTARY", "MAINTAIN"));
        NutritionProfileDto tallerActive = profileService.saveProfile("b@fitkit.local",
                profile(40, "MALE", 190, 95, "HIGH", "GAIN"));
        assertNotEquals(smallerSedentary.getDailyCalories(), tallerActive.getDailyCalories());
    }

    @Test
    void rejectsProfilesOutsideAdultSupportedRange() {
        NutritionProfileDto underage = profile(17, "FEMALE", 165, 60, "LIGHT", "MAINTAIN");
        assertThrows(IllegalArgumentException.class,
                () -> profileService.saveProfile("minor@fitkit.local", underage));
    }

    private NutritionProfileDto profile(int age, String sex, int height, double weight, String activity, String goal) {
        NutritionProfileDto profile = new NutritionProfileDto();
        profile.setAge(age);
        profile.setSexForEstimate(sex);
        profile.setHeightCm(height);
        profile.setWeightKg(weight);
        profile.setActivityLevel(activity);
        profile.setGoal(goal);
        return profile;
    }
}
