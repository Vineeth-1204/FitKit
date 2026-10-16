package com.fitkit.repository;

import com.fitkit.domain.entity.FoodNutrient;
import com.fitkit.domain.enums.NutrientBasis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FoodNutrientRepository extends JpaRepository<FoodNutrient, Long> {
    Optional<FoodNutrient> findByFoodIdAndBasis(Long foodId, NutrientBasis basis);
}
