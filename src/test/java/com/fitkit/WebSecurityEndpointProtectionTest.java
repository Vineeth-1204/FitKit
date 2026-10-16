package com.fitkit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class WebSecurityEndpointProtectionTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should permit unauthenticated access to public catalog, recipes, and stats")
    void testPublicEndpointsPermitted() throws Exception {
        mockMvc.perform(get("/api/foods"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/recipes"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/stats"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should reject unauthenticated requests to protected meals endpoint with 401")
    void testProtectedMealsEndpointRejection() throws Exception {
        mockMvc.perform(get("/api/meals"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should reject unauthenticated requests to protected targets endpoint with 401")
    void testProtectedTargetsEndpointRejection() throws Exception {
        mockMvc.perform(get("/api/targets"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should reject unauthenticated requests to the nutrition onboarding endpoint")
    void testNutritionProfileEndpointRejection() throws Exception {
        mockMvc.perform(get("/api/nutrition-profile"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(put("/api/nutrition-profile").with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "demo@fitkit.local")
    @DisplayName("Should allow authenticated access to meals endpoint")
    void testAuthenticatedAccessToMeals() throws Exception {
        mockMvc.perform(get("/api/meals"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "demo@fitkit.local")
    @DisplayName("Should allow authenticated access to targets endpoint")
    void testAuthenticatedAccessToTargets() throws Exception {
        mockMvc.perform(get("/api/targets"))
                .andExpect(status().isOk());
    }
}
