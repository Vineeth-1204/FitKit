package com.fitkit.repository;

import com.fitkit.domain.entity.SourceProvenance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SourceProvenanceRepository extends JpaRepository<SourceProvenance, String> {
}
