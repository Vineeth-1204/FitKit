package com.fitkit.controller;

import com.fitkit.dto.NutritionProfileDto;
import com.fitkit.service.NutritionProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

@RestController
@RequestMapping("/api/nutrition-profile")
public class NutritionProfileController {
    private final NutritionProfileService service;

    public NutritionProfileController(NutritionProfileService service) { this.service = service; }

    @GetMapping
    public ResponseEntity<NutritionProfileDto> getProfile(Principal principal) {
        if (principal == null || principal.getName() == null) {
            return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(service.getProfile(principal.getName()));
    }

    @PutMapping
    public ResponseEntity<NutritionProfileDto> saveProfile(@Valid @RequestBody NutritionProfileDto profile, Principal principal) {
        if (principal == null || principal.getName() == null) {
            return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(service.saveProfile(principal.getName(), profile));
    }
}
