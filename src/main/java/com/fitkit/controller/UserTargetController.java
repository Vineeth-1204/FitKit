package com.fitkit.controller;

import com.fitkit.dto.UserTargetDto;
import com.fitkit.service.UserTargetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/targets")
public class UserTargetController {

    private final UserTargetService userTargetService;

    public UserTargetController(UserTargetService userTargetService) {
        this.userTargetService = userTargetService;
    }

    @GetMapping
    public ResponseEntity<UserTargetDto> getTargets(Principal principal) {
        String userId = principal.getName();
        return ResponseEntity.ok(userTargetService.getTargets(userId));
    }

    @PutMapping
    public ResponseEntity<UserTargetDto> saveTargets(@RequestBody UserTargetDto dto, Principal principal) {
        String userId = principal.getName();
        return ResponseEntity.ok(userTargetService.saveTargets(userId, dto));
    }
}
