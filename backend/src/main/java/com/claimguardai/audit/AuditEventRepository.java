package com.claimguardai.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {

    Page<AuditEvent> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<AuditEvent> findByActorUserIdOrderByCreatedAtDesc(Long actorUserId, Pageable pageable);

    @Query("""
            SELECT a FROM AuditEvent a
            WHERE (:eventType IS NULL OR a.eventType = :eventType)
            ORDER BY a.createdAt DESC
            """)
    Page<AuditEvent> findFiltered(
            @Param("eventType") AuditEventType eventType,
            Pageable pageable);
}
