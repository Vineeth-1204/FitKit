package com.fitkit.repository;

import com.fitkit.domain.entity.FoodPortion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodPortionRepository extends JpaRepository<FoodPortion, Long> {
    List<FoodPortion> findByFoodId(Long foodId);
}
