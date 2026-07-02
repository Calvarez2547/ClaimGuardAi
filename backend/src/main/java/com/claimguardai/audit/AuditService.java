package com.claimguardai.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditEventRepository repo;

    public AuditService(AuditEventRepository repo) {
        this.repo = repo;
    }

    @Async
    public void log(
            AuditEventType eventType,
            Long actorUserId,
            String targetEntity,
            String targetId,
            String ipAddress,
            String correlationId,
            String description) {

        try {
            AuditEvent event = new AuditEvent();
            event.setEventType(eventType);
            event.setActorUserId(actorUserId);
            event.setTargetEntity(targetEntity);
            event.setTargetId(targetId);
            event.setIpAddress(ipAddress);
            event.setCorrelationId(correlationId);
            event.setDescription(description);
            repo.save(event);
        } catch (Exception e) {
            log.error("Failed to persist audit event {}: {}", eventType, e.getMessage());
        }
    }

    @Async
    public void log(AuditEventType eventType, Long actorUserId, String description) {
        log(eventType, actorUserId, null, null, null, null, description);
    }

    @Async
    public void log(AuditEventType eventType, String description) {
        log(eventType, null, null, null, null, null, description);
    }
}
