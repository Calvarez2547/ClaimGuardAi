package com.claimguardai.claims;

import com.claimguardai.dashboard.ClaimStatusSummaryResponse;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClaimRepository extends JpaRepository<Claim, Long> {

    long countByCreatedById(Long createdById);

    Optional<Claim> findByIdAndCreatedById(Long id, Long createdById);

    List<Claim> findByCreatedByIdOrderByCreatedAtDescIdDesc(Long createdById);

    List<Claim> findByCreatedByIdOrderByCreatedAtDescIdDesc(Long createdById, Pageable pageable);

    @Query("""
            select new com.claimguardai.dashboard.ClaimStatusSummaryResponse(c.claimStatus, count(c))
            from Claim c
            where c.createdBy.id = :createdById
            group by c.claimStatus
            """)
    List<ClaimStatusSummaryResponse> summarizeByStatus(@Param("createdById") Long createdById);
}
