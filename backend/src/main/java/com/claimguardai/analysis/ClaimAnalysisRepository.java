package com.claimguardai.analysis;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClaimAnalysisRepository extends JpaRepository<ClaimAnalysis, Long> {

    Optional<ClaimAnalysis> findFirstByClaimIdAndClaimCreatedByIdOrderByCreatedAtDescIdDesc(
            Long claimId,
            Long createdById);

    List<ClaimAnalysis> findByClaimIdAndClaimCreatedByIdOrderByCreatedAtDescIdDesc(
            Long claimId,
            Long createdById);
}
