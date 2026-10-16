package com.fitkit.repository;

import com.fitkit.domain.entity.ImportAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImportAuditLogRepository extends JpaRepository<ImportAuditLog, Long> {
    List<ImportAuditLog> findBySourceIdOrderByImportTimestampDesc(String sourceId);
}
