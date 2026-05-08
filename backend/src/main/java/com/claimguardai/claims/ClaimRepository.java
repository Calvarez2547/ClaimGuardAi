package com.claimguardai.claims;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClaimRepository extends JpaRepository<Claim, Long> {

    Optional<Claim> findByIdAndCreatedById(Long id, Long createdById);

    List<Claim> findByCreatedByIdOrderByCreatedAtDescIdDesc(Long createdById);
}
