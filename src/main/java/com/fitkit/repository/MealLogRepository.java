package com.fitkit.repository;

import com.fitkit.domain.entity.MealLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MealLogRepository extends JpaRepository<MealLog, Long> {

    Optional<MealLog> findByUserIdAndLogDate(String userId, LocalDate logDate);

    @Query("SELECT DISTINCT m FROM MealLog m LEFT JOIN FETCH m.entries WHERE m.userId = :userId AND m.logDate = :logDate")
    Optional<MealLog> findByUserIdAndLogDateWithEntries(@Param("userId") String userId, @Param("logDate") LocalDate logDate);

    @Query("SELECT DISTINCT m FROM MealLog m LEFT JOIN FETCH m.entries WHERE m.userId = :userId AND m.logDate BETWEEN :startDate AND :endDate ORDER BY m.logDate ASC")
    List<MealLog> findHistoryWithEntries(@Param("userId") String userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
