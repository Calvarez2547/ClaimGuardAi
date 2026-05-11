package com.claimguardai.analysis;

import com.claimguardai.dashboard.CommonRiskFactorResponse;
import com.claimguardai.dashboard.LatestClaimAnalysisView;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClaimAnalysisRepository extends JpaRepository<ClaimAnalysis, Long> {

    Optional<ClaimAnalysis> findFirstByClaimIdAndClaimCreatedByIdOrderByCreatedAtDescIdDesc(
            Long claimId,
            Long createdById);

    List<ClaimAnalysis> findByClaimIdAndClaimCreatedByIdOrderByCreatedAtDescIdDesc(
            Long claimId,
            Long createdById);

    @Query("""
            select new com.claimguardai.dashboard.LatestClaimAnalysisView(
                a.id,
                c.id,
                c.claimNumber,
                c.patientControlNumber,
                c.claimStatus,
                c.billedAmount,
                a.riskScore,
                a.riskCategory,
                a.primaryRiskReason,
                a.humanReviewRequired,
                c.createdAt,
                c.updatedAt,
                a.createdAt
            )
            from ClaimAnalysis a
            join a.claim c
            where c.createdBy.id = :createdById
              and not exists (
                  select 1
                  from ClaimAnalysis newer
                  where newer.claim = a.claim
                    and (
                        newer.createdAt > a.createdAt
                        or (newer.createdAt = a.createdAt and newer.id > a.id)
                    )
              )
            order by a.createdAt desc, a.id desc
            """)
    List<LatestClaimAnalysisView> findLatestAnalysisViewsByClaimCreatedById(
            @Param("createdById") Long createdById);

    @Query("""
            select new com.claimguardai.dashboard.CommonRiskFactorResponse(
                f.findingCode,
                f.factorCategory,
                f.factorLabel,
                count(f),
                coalesce(sum(f.contribution), 0)
            )
            from ClaimAnalysisFinding f
            join f.analysis a
            join a.claim c
            where c.createdBy.id = :createdById
              and not exists (
                  select 1
                  from ClaimAnalysis newer
                  where newer.claim = a.claim
                    and (
                        newer.createdAt > a.createdAt
                        or (newer.createdAt = a.createdAt and newer.id > a.id)
                    )
              )
            group by f.findingCode, f.factorCategory, f.factorLabel
            order by count(f) desc, coalesce(sum(f.contribution), 0) desc, f.findingCode asc
            """)
    List<CommonRiskFactorResponse> findTopRiskFactorsForLatestAnalysesByClaimCreatedById(
            @Param("createdById") Long createdById);
}
