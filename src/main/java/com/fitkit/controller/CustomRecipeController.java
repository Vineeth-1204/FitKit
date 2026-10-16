package com.fitkit.controller;

import com.fitkit.dto.CustomRecipeRequestDto;
import com.fitkit.dto.RecipeCalculationResultDto;
import com.fitkit.dto.RecipeDetailDto;
import com.fitkit.service.CustomRecipeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/custom-recipes")
public class CustomRecipeController {

    private final CustomRecipeService customRecipeService;

    public CustomRecipeController(CustomRecipeService customRecipeService) {
        this.customRecipeService = customRecipeService;
    }

    @PostMapping
    public ResponseEntity<RecipeDetailDto> createCustomRecipe(
            @Valid @RequestBody CustomRecipeRequestDto request,
            Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        RecipeDetailDto created = customRecipeService.createCustomRecipe(principal.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<RecipeDetailDto>> getUserCustomRecipes(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<RecipeDetailDto> recipes = customRecipeService.getUserCustomRecipes(principal.getName());
        return ResponseEntity.ok(recipes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecipeDetailDto> getCustomRecipeById(
            @PathVariable Long id,
            Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            RecipeDetailDto recipe = customRecipeService.getCustomRecipeById(principal.getName(), id);
            return ResponseEntity.ok(recipe);
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecipeDetailDto> updateCustomRecipe(
            @PathVariable Long id,
            @Valid @RequestBody CustomRecipeRequestDto request,
            Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            RecipeDetailDto updated = customRecipeService.updateCustomRecipe(principal.getName(), id, request);
            return ResponseEntity.ok(updated);
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteCustomRecipe(
            @PathVariable Long id,
            Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            customRecipeService.deleteCustomRecipe(principal.getName(), id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Custom recipe deleted successfully"));
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PostMapping("/preview")
    public ResponseEntity<RecipeCalculationResultDto> calculatePreview(
            @Valid @RequestBody CustomRecipeRequestDto request) {
        RecipeCalculationResultDto preview = customRecipeService.calculatePreview(request);
        return ResponseEntity.ok(preview);
    }
}
