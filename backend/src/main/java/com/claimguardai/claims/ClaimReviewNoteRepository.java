package com.claimguardai.claims;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClaimReviewNoteRepository extends JpaRepository<ClaimReviewNote, Long> {

    List<ClaimReviewNote> findByClaimIdAndClaimCreatedByIdOrderByCreatedAtAscIdAsc(Long claimId, Long createdById);
}
