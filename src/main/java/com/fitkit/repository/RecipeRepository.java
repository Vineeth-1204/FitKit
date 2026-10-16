package com.fitkit.repository;

import com.fitkit.domain.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    Optional<Recipe> findByRecipeCode(String recipeCode);
    Optional<Recipe> findByFoodId(Long foodId);

    @Query("SELECT DISTINCT r FROM Recipe r LEFT JOIN FETCH r.ingredients i LEFT JOIN FETCH i.food f WHERE (r.isCustom = false OR r.userId IS NULL) AND r.isActive = true")
    List<Recipe> findAllWithIngredients();

    @Query("SELECT r FROM Recipe r LEFT JOIN FETCH r.ingredients i LEFT JOIN FETCH i.food f WHERE r.id = :id")
    Optional<Recipe> findByIdWithIngredients(@Param("id") Long id);

    @Query("SELECT DISTINCT r FROM Recipe r LEFT JOIN FETCH r.ingredients i LEFT JOIN FETCH i.food f WHERE r.userId = :userId AND r.isCustom = true AND r.isActive = true ORDER BY r.id DESC")
    List<Recipe> findByUserIdWithIngredients(@Param("userId") String userId);

    @Query("SELECT r FROM Recipe r LEFT JOIN FETCH r.ingredients i LEFT JOIN FETCH i.food f WHERE r.id = :id AND r.userId = :userId AND r.isActive = true")
    Optional<Recipe> findByIdAndUserIdWithIngredients(@Param("id") Long id, @Param("userId") String userId);

    @Query("SELECT r FROM Recipe r LEFT JOIN FETCH r.ingredients i LEFT JOIN FETCH i.food f WHERE r.id = :id AND r.isActive = true AND (r.isCustom = false OR r.userId IS NULL OR r.userId = :userId)")
    Optional<Recipe> findByIdAccessible(@Param("id") Long id, @Param("userId") String userId);
}
