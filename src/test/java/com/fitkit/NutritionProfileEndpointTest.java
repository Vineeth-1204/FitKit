package com.fitkit;

import com.fitkit.domain.entity.NutritionProfile;
import com.fitkit.domain.entity.UserTarget;
import com.fitkit.dto.RegisterRequestDto;
import com.fitkit.repository.NutritionProfileRepository;
import com.fitkit.repository.UserTargetRepository;
import com.fitkit.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class NutritionProfileEndpointTest {

    @Autowired MockMvc mockMvc;
    @Autowired AuthService authService;
    @Autowired NutritionProfileRepository nutritionProfileRepository;
    @Autowired UserTargetRepository userTargetRepository;

    @Test
    @DisplayName("Unauthenticated request to GET /api/nutrition-profile returns 401")
    void unauthenticatedGetReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/nutrition-profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Unauthenticated request to PUT /api/nutrition-profile returns 401")
    void unauthenticatedPutReturnsUnauthorized() throws Exception {
        mockMvc.perform(put("/api/nutrition-profile")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"age":20,"sexForEstimate":"MALE","heightCm":174,"weightKg":66.0,"activityLevel":"SEDENTARY","goal":"LOSE"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "newuser@fitkit.local")
    @DisplayName("New user without profile receives incomplete profile and null targets")
    void authenticatedGetBeforeOnboardingReturnsIncompleteProfile() throws Exception {
        authService.register(new RegisterRequestDto("newuser@fitkit.local", "StrongPassword1!", "New User"));

        mockMvc.perform(get("/api/nutrition-profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.complete").value(false))
                .andExpect(jsonPath("$.dailyCalories").doesNotExist());

        mockMvc.perform(get("/api/targets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dailyCalories").doesNotExist())
                .andExpect(jsonPath("$.proteinG").doesNotExist());
    }

    @Test
    @WithMockUser(username = "exact.screenshot@fitkit.local")
    @DisplayName("Exact failing request from screenshot calculates personalized targets and persists profile")
    void exactFailingRequestSucceedsAndCalculatesPersonalizedTargets() throws Exception {
        authService.register(new RegisterRequestDto("exact.screenshot@fitkit.local", "StrongPassword1!", "Screenshot User"));

        // Exact inputs from user's screenshot: Age 20, Male, 174cm, 66kg, SEDENTARY, LOSE
        mockMvc.perform(put("/api/nutrition-profile")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "age": 20,
                                    "sexForEstimate": "MALE",
                                    "heightCm": 174,
                                    "weightKg": 66.0,
                                    "activityLevel": "SEDENTARY",
                                    "goal": "LOSE"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.complete").value(true))
                .andExpect(jsonPath("$.dailyCalories").value(1700))
                .andExpect(jsonPath("$.proteinG").value(52.8))
                .andExpect(jsonPath("$.carbsG").value(244.6))
                .andExpect(jsonPath("$.fatG").value(56.7))
                .andExpect(jsonPath("$.fiberG").value(23.8));

        // Verify persistence in NutritionProfile table
        Optional<NutritionProfile> profileOpt = nutritionProfileRepository.findByUserId("exact.screenshot@fitkit.local");
        assertThat(profileOpt).isPresent();
        NutritionProfile profile = profileOpt.get();
        assertThat(profile.getAge()).isEqualTo(20);
        assertThat(profile.getSexForEstimate()).isEqualTo("MALE");
        assertThat(profile.getHeightCm()).isEqualTo(174);
        assertThat(profile.getWeightKg()).isEqualTo(66.0);
        assertThat(profile.getActivityLevel()).isEqualTo("SEDENTARY");
        assertThat(profile.getGoal()).isEqualTo("LOSE");

        // Verify persistence in UserTarget table
        Optional<UserTarget> targetOpt = userTargetRepository.findByUserId("exact.screenshot@fitkit.local");
        assertThat(targetOpt).isPresent();
        UserTarget target = targetOpt.get();
        assertThat(target.getDailyCalories()).isEqualTo(1700);
        assertThat(target.getProteinG()).isEqualByComparingTo(new BigDecimal("52.8"));

        // Subsequent GET /api/nutrition-profile returns complete profile with targets
        mockMvc.perform(get("/api/nutrition-profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.complete").value(true))
                .andExpect(jsonPath("$.dailyCalories").value(1700));

        // Subsequent GET /api/targets returns the personalized targets
        mockMvc.perform(get("/api/targets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dailyCalories").value(1700))
                .andExpect(jsonPath("$.proteinG").value(52.8));
    }

    @Test
    @WithMockUser(username = "settings.edit@fitkit.local")
    @DisplayName("Settings target updates work and can coexist with onboarding profile")
    void settingsTargetEditWorksIndependently() throws Exception {
        authService.register(new RegisterRequestDto("settings.edit@fitkit.local", "StrongPassword1!", "Settings User"));

        // Complete onboarding first
        mockMvc.perform(put("/api/nutrition-profile")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"age":25,"sexForEstimate":"FEMALE","heightCm":165,"weightKg":60.0,"activityLevel":"MODERATE","goal":"MAINTAIN"}
                                """))
                .andExpect(status().isOk());

        // Update target via Settings /api/targets
        mockMvc.perform(put("/api/targets")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "dailyCalories": 2200,
                                    "proteinG": 120.0,
                                    "carbsG": 250.0,
                                    "fatG": 70.0,
                                    "fiberG": 30.0
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dailyCalories").value(2200));

        // Verify GET /api/targets reflects settings change
        mockMvc.perform(get("/api/targets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dailyCalories").value(2200))
                .andExpect(jsonPath("$.proteinG").value(120.0));
    }

    @Test
    @WithMockUser(username = "invalid.data@fitkit.local")
    @DisplayName("Invalid questionnaire fields are rejected with 400 Bad Request")
    void invalidQuestionnaireRejected() throws Exception {
        authService.register(new RegisterRequestDto("invalid.data@fitkit.local", "StrongPassword1!", "Invalid Data"));

        // Age under 18
        mockMvc.perform(put("/api/nutrition-profile")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"age":16,"sexForEstimate":"MALE","heightCm":174,"weightKg":66.0,"activityLevel":"SEDENTARY","goal":"LOSE"}
                                """))
                .andExpect(status().isBadRequest());

        // Weight out of range
        mockMvc.perform(put("/api/nutrition-profile")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"age":25,"sexForEstimate":"MALE","heightCm":174,"weightKg":20.0,"activityLevel":"SEDENTARY","goal":"LOSE"}
                                """))
                .andExpect(status().isBadRequest());
    }
}
