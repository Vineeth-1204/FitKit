package com.fitkit.repository;

import com.fitkit.domain.entity.UserTarget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserTargetRepository extends JpaRepository<UserTarget, Long> {
    Optional<UserTarget> findByUserId(String userId);
}
