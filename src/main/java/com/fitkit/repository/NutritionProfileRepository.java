package com.fitkit.repository;

import com.fitkit.domain.entity.NutritionProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface NutritionProfileRepository extends JpaRepository<NutritionProfile, Long> {
    Optional<NutritionProfile> findByUserId(String userId);
}
