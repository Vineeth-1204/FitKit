package com.fitkit.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "source_provenance")
public class SourceProvenance {

    @Id
    @Column(name = "id", length = 50, nullable = false)
    private String id;

    @Column(name = "dataset_name", length = 150, nullable = false)
    private String datasetName;

    @Column(name = "publisher", length = 255, nullable = false)
    private String publisher;

    @Column(name = "version_or_date", length = 50, nullable = false)
    private String versionOrDate;

    @Column(name = "license_type", length = 100, nullable = false)
    private String licenseType;

    @Column(name = "official_url", length = 500)
    private String officialUrl;

    @Column(name = "is_commercial_allowed")
    private Boolean isCommercialAllowed = false;

    @Column(name = "citation_text", columnDefinition = "TEXT")
    private String citationText;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public SourceProvenance() {}

    public SourceProvenance(String id, String datasetName, String publisher, String versionOrDate, String licenseType, String officialUrl, Boolean isCommercialAllowed, String citationText) {
        this.id = id;
        this.datasetName = datasetName;
        this.publisher = publisher;
        this.versionOrDate = versionOrDate;
        this.licenseType = licenseType;
        this.officialUrl = officialUrl;
        this.isCommercialAllowed = isCommercialAllowed;
        this.citationText = citationText;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDatasetName() { return datasetName; }
    public void setDatasetName(String datasetName) { this.datasetName = datasetName; }

    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }

    public String getVersionOrDate() { return versionOrDate; }
    public void setVersionOrDate(String versionOrDate) { this.versionOrDate = versionOrDate; }

    public String getLicenseType() { return licenseType; }
    public void setLicenseType(String licenseType) { this.licenseType = licenseType; }

    public String getOfficialUrl() { return officialUrl; }
    public void setOfficialUrl(String officialUrl) { this.officialUrl = officialUrl; }

    public Boolean getCommercialAllowed() { return isCommercialAllowed; }
    public void setCommercialAllowed(Boolean commercialAllowed) { isCommercialAllowed = commercialAllowed; }

    public String getCitationText() { return citationText; }
    public void setCitationText(String citationText) { this.citationText = citationText; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
