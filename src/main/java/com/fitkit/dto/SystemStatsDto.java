package com.fitkit.dto;

import java.util.List;

public class SystemStatsDto {
    private long totalFoods;
    private long verifiedFoods;
    private long totalRecipes;
    private long totalNutrientProfiles;
    private long totalPortions;
    private List<SourceSummary> sources;
    private List<AuditLogSummary> recentAuditLogs;

    public static class SourceSummary {
        private String id;
        private String name;
        private String publisher;
        private String defaultLicense;
        private long foodCount;

        public SourceSummary() {}
        public SourceSummary(String id, String name, String publisher, String defaultLicense, long foodCount) {
            this.id = id;
            this.name = name;
            this.publisher = publisher;
            this.defaultLicense = defaultLicense;
            this.foodCount = foodCount;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getPublisher() { return publisher; }
        public void setPublisher(String publisher) { this.publisher = publisher; }
        public String getDefaultLicense() { return defaultLicense; }
        public void setDefaultLicense(String defaultLicense) { this.defaultLicense = defaultLicense; }
        public long getFoodCount() { return foodCount; }
        public void setFoodCount(long foodCount) { this.foodCount = foodCount; }
    }

    public static class AuditLogSummary {
        private Long id;
        private String batchId;
        private String sourceName;
        private int totalRecords;
        private int successCount;
        private int skippedDuplicates;
        private int anomalyCorrections;
        private String status;
        private String executedAt;

        public AuditLogSummary() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getBatchId() { return batchId; }
        public void setBatchId(String batchId) { this.batchId = batchId; }
        public String getSourceName() { return sourceName; }
        public void setSourceName(String sourceName) { this.sourceName = sourceName; }
        public int getTotalRecords() { return totalRecords; }
        public void setTotalRecords(int totalRecords) { this.totalRecords = totalRecords; }
        public int getSuccessCount() { return successCount; }
        public void setSuccessCount(int successCount) { this.successCount = successCount; }
        public int getSkippedDuplicates() { return skippedDuplicates; }
        public void setSkippedDuplicates(int skippedDuplicates) { this.skippedDuplicates = skippedDuplicates; }
        public int getAnomalyCorrections() { return anomalyCorrections; }
        public void setAnomalyCorrections(int anomalyCorrections) { this.anomalyCorrections = anomalyCorrections; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getExecutedAt() { return executedAt; }
        public void setExecutedAt(String executedAt) { this.executedAt = executedAt; }
    }

    public SystemStatsDto() {}

    public long getTotalFoods() { return totalFoods; }
    public void setTotalFoods(long totalFoods) { this.totalFoods = totalFoods; }

    public long getVerifiedFoods() { return verifiedFoods; }
    public void setVerifiedFoods(long verifiedFoods) { this.verifiedFoods = verifiedFoods; }

    public long getTotalRecipes() { return totalRecipes; }
    public void setTotalRecipes(long totalRecipes) { this.totalRecipes = totalRecipes; }

    public long getTotalNutrientProfiles() { return totalNutrientProfiles; }
    public void setTotalNutrientProfiles(long totalNutrientProfiles) { this.totalNutrientProfiles = totalNutrientProfiles; }

    public long getTotalPortions() { return totalPortions; }
    public void setTotalPortions(long totalPortions) { this.totalPortions = totalPortions; }

    public List<SourceSummary> getSources() { return sources; }
    public void setSources(List<SourceSummary> sources) { this.sources = sources; }

    public List<AuditLogSummary> getRecentAuditLogs() { return recentAuditLogs; }
    public void setRecentAuditLogs(List<AuditLogSummary> recentAuditLogs) { this.recentAuditLogs = recentAuditLogs; }
}
