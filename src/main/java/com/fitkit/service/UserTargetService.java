package com.fitkit.service;

import com.fitkit.domain.entity.UserTarget;
import com.fitkit.dto.UserTargetDto;
import com.fitkit.repository.UserTargetRepository;
import com.fitkit.repository.NutritionProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class UserTargetService {

    private final UserTargetRepository userTargetRepository;
    private final NutritionProfileRepository nutritionProfileRepository;

    public UserTargetService(UserTargetRepository userTargetRepository, NutritionProfileRepository nutritionProfileRepository) {
        this.userTargetRepository = userTargetRepository;
        this.nutritionProfileRepository = nutritionProfileRepository;
    }

    @Transactional(readOnly = true)
    public UserTargetDto getTargets(String userId) {
        String uid = (userId == null || userId.trim().isEmpty()) ? "default-user" : userId.trim();
        if (nutritionProfileRepository.findByUserId(uid).isEmpty()) {
            return new UserTargetDto(uid, null, null, null, null, null);
        }
        return userTargetRepository.findByUserId(uid)
                .map(this::mapToDto)
                .orElseGet(() -> new UserTargetDto(uid, null, null, null, null, null));
    }

    @Transactional
    public UserTargetDto saveTargets(String userId, UserTargetDto dto) {
        String uid = (userId == null || userId.trim().isEmpty()) ? "default-user" : userId.trim();
        UserTarget target = userTargetRepository.findByUserId(uid)
                .orElseGet(() -> {
                    UserTarget ut = new UserTarget();
                    ut.setUserId(uid);
                    return ut;
                });

        if (dto.getDailyCalories() == null || dto.getDailyCalories() < 1200 || dto.getDailyCalories() > 5000
                || dto.getProteinG() == null || dto.getProteinG().signum() <= 0
                || dto.getCarbsG() == null || dto.getCarbsG().signum() <= 0
                || dto.getFatG() == null || dto.getFatG().signum() <= 0
                || dto.getFiberG() == null || dto.getFiberG().signum() <= 0) {
            throw new IllegalArgumentException("Daily calorie target must be 1,200–5,000 and all macronutrient targets must be positive.");
        }

        if (dto.getDailyCalories() != null && dto.getDailyCalories() > 0) {
            target.setDailyCalories(dto.getDailyCalories());
        }
        if (dto.getProteinG() != null && dto.getProteinG().compareTo(BigDecimal.ZERO) >= 0) {
            target.setProteinG(dto.getProteinG());
        }
        if (dto.getCarbsG() != null && dto.getCarbsG().compareTo(BigDecimal.ZERO) >= 0) {
            target.setCarbsG(dto.getCarbsG());
        }
        if (dto.getFatG() != null && dto.getFatG().compareTo(BigDecimal.ZERO) >= 0) {
            target.setFatG(dto.getFatG());
        }
        if (dto.getFiberG() != null && dto.getFiberG().compareTo(BigDecimal.ZERO) >= 0) {
            target.setFiberG(dto.getFiberG());
        }

        UserTarget saved = userTargetRepository.save(target);
        return mapToDto(saved);
    }

    private UserTargetDto mapToDto(UserTarget target) {
        return new UserTargetDto(
                target.getUserId(),
                target.getDailyCalories(),
                target.getProteinG(),
                target.getCarbsG(),
                target.getFatG(),
                target.getFiberG()
        );
    }
}
