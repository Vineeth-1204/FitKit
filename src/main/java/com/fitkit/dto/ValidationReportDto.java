package com.fitkit.dto;

import java.util.ArrayList;
import java.util.List;

public class ValidationReportDto {
    private String datasetName;
    private int totalRead = 0;
    private int importedCount = 0;
    private int skippedDuplicates = 0;
    private int correctedCount = 0;
    private int rejectedCount = 0;
    private List<String> anomalyAuditLogs = new ArrayList<>();
    private List<String> rejectionReasons = new ArrayList<>();

    public ValidationReportDto() {}
    public ValidationReportDto(String datasetName) {
        this.datasetName = datasetName;
    }

    public void incrementRead() { totalRead++; }
    public void incrementImported() { importedCount++; }
    public void incrementSkipped() { skippedDuplicates++; }
    public void incrementCorrected(String log) {
        correctedCount++;
        anomalyAuditLogs.add(log);
    }
    public void incrementRejected(String reason) {
        rejectedCount++;
        rejectionReasons.add(reason);
    }

    public String getDatasetName() { return datasetName; }
    public void setDatasetName(String datasetName) { this.datasetName = datasetName; }

    public int getTotalRead() { return totalRead; }
    public void setTotalRead(int totalRead) { this.totalRead = totalRead; }

    public int getImportedCount() { return importedCount; }
    public void setImportedCount(int importedCount) { this.importedCount = importedCount; }

    public int getSkippedDuplicates() { return skippedDuplicates; }
    public void setSkippedDuplicates(int skippedDuplicates) { this.skippedDuplicates = skippedDuplicates; }

    public int getCorrectedCount() { return correctedCount; }
    public void setCorrectedCount(int correctedCount) { this.correctedCount = correctedCount; }

    public int getRejectedCount() { return rejectedCount; }
    public void setRejectedCount(int rejectedCount) { this.rejectedCount = rejectedCount; }

    public List<String> getAnomalyAuditLogs() { return anomalyAuditLogs; }
    public void setAnomalyAuditLogs(List<String> anomalyAuditLogs) { this.anomalyAuditLogs = anomalyAuditLogs; }

    public List<String> getRejectionReasons() { return rejectionReasons; }
    public void setRejectionReasons(List<String> rejectionReasons) { this.rejectionReasons = rejectionReasons; }

    @Override
    public String toString() {
        return "ValidationReport for [" + datasetName + "]: Read=" + totalRead +
                ", Imported=" + importedCount +
                ", Skipped=" + skippedDuplicates +
                ", Corrected=" + correctedCount +
                ", Rejected=" + rejectedCount;
    }
}
