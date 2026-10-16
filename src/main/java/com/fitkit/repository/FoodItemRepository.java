package com.fitkit.repository;

import com.fitkit.domain.entity.FoodItem;
import com.fitkit.domain.enums.FoodState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FoodItemRepository extends JpaRepository<FoodItem, Long> {

    Optional<FoodItem> findByFoodCode(String foodCode);

    Optional<FoodItem> findBySourceIdAndSourceRecordId(String sourceId, String sourceRecordId);

    Optional<FoodItem> findByBarcode(String barcode);

    List<FoodItem> findByFoodState(FoodState foodState);

    @Query("SELECT f FROM FoodItem f WHERE LOWER(f.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<FoodItem> searchByNameLike(@Param("query") String query);

    @Query("SELECT DISTINCT f.foodGroup FROM FoodItem f WHERE f.foodGroup IS NOT NULL ORDER BY f.foodGroup ASC")
    List<String> findDistinctFoodGroups();

    @Query("SELECT DISTINCT f FROM FoodItem f LEFT JOIN FETCH f.source s")
    List<FoodItem> findAllWithDetails();

    @Query("SELECT DISTINCT f FROM FoodItem f LEFT JOIN FETCH f.source s LEFT JOIN FETCH f.aliases a")
    List<FoodItem> findAllWithAliasesAndSource();

    @Query("SELECT DISTINCT f FROM FoodItem f LEFT JOIN FETCH f.source s WHERE f.id = :id")
    Optional<FoodItem> findByIdWithDetails(@Param("id") Long id);
}
