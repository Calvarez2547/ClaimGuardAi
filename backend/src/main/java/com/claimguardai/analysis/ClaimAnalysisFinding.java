package com.claimguardai.analysis;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "claim_analysis_findings")
public class ClaimAnalysisFinding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "analysis_id", nullable = false)
    private ClaimAnalysis analysis;

    @Column(name = "finding_code", nullable = false, length = 80)
    private String findingCode;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false)
    private int points;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public Long getId() {
        return id;
    }

    public ClaimAnalysis getAnalysis() {
        return analysis;
    }

    public void setAnalysis(ClaimAnalysis analysis) {
        this.analysis = analysis;
    }

    public String getFindingCode() {
        return findingCode;
    }

    public void setFindingCode(String findingCode) {
        this.findingCode = findingCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
