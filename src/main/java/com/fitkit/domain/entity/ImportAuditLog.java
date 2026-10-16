package com.fitkit.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "import_audit_logs")
public class ImportAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id", nullable = false)
    private SourceProvenance source;

    @Column(name = "import_timestamp")
    private LocalDateTime importTimestamp = LocalDateTime.now();

    @Column(name = "records_read", nullable = false)
    private Integer recordsRead = 0;

    @Column(name = "records_imported", nullable = false)
    private Integer recordsImported = 0;

    @Column(name = "records_skipped_duplicate", nullable = false)
    private Integer recordsSkippedDuplicate = 0;

    @Column(name = "records_corrected", nullable = false)
    private Integer recordsCorrected = 0;

    @Column(name = "records_rejected_invalid", nullable = false)
    private Integer recordsRejectedInvalid = 0;

    @Column(name = "log_summary", columnDefinition = "TEXT")
    private String logSummary;

    public ImportAuditLog() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public SourceProvenance getSource() { return source; }
    public void setSource(SourceProvenance source) { this.source = source; }

    public LocalDateTime getImportTimestamp() { return importTimestamp; }
    public void setImportTimestamp(LocalDateTime importTimestamp) { this.importTimestamp = importTimestamp; }

    public Integer getRecordsRead() { return recordsRead; }
    public void setRecordsRead(Integer recordsRead) { this.recordsRead = recordsRead; }

    public Integer getRecordsImported() { return recordsImported; }
    public void setRecordsImported(Integer recordsImported) { this.recordsImported = recordsImported; }

    public Integer getRecordsSkippedDuplicate() { return recordsSkippedDuplicate; }
    public void setRecordsSkippedDuplicate(Integer recordsSkippedDuplicate) { this.recordsSkippedDuplicate = recordsSkippedDuplicate; }

    public Integer getRecordsCorrected() { return recordsCorrected; }
    public void setRecordsCorrected(Integer recordsCorrected) { this.recordsCorrected = recordsCorrected; }

    public Integer getRecordsRejectedInvalid() { return recordsRejectedInvalid; }
    public void setRecordsRejectedInvalid(Integer recordsRejectedInvalid) { this.recordsRejectedInvalid = recordsRejectedInvalid; }

    public String getLogSummary() { return logSummary; }
    public void setLogSummary(String logSummary) { this.logSummary = logSummary; }
}
