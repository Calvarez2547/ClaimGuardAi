package com.claimguardai.analysis;

import com.claimguardai.scoring.RiskFactorCategory;
import com.claimguardai.scoring.RiskFactorSeverity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "factor_category", nullable = false, length = 50)
    private RiskFactorCategory factorCategory;

    @Column(name = "factor_label", nullable = false, length = 120)
    private String factorLabel;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 20)
    private RiskFactorSeverity severity;

    @Column(nullable = false)
    private int weight;

    @Column(nullable = false)
    private int contribution;

    @Column(name = "recommended_action", nullable = false, length = 500)
    private String recommendedAction;

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

    public RiskFactorCategory getFactorCategory() {
        return factorCategory;
    }

    public void setFactorCategory(RiskFactorCategory factorCategory) {
        this.factorCategory = factorCategory;
    }

    public String getFactorLabel() {
        return factorLabel;
    }

    public void setFactorLabel(String factorLabel) {
        this.factorLabel = factorLabel;
    }

    public RiskFactorSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(RiskFactorSeverity severity) {
        this.severity = severity;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getContribution() {
        return contribution;
    }

    public void setContribution(int contribution) {
        this.contribution = contribution;
    }

    public String getRecommendedAction() {
        return recommendedAction;
    }

    public void setRecommendedAction(String recommendedAction) {
        this.recommendedAction = recommendedAction;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
