package com.claimguardai.audit;

import java.time.Instant;

public record AuditEventResponse(
        Long id,
        String eventType,
        Long actorUserId,
        String targetEntity,
        String targetId,
        String ipAddress,
        String correlationId,
        String description,
        Instant createdAt) {

    public static AuditEventResponse from(AuditEvent event) {
        return new AuditEventResponse(
                event.getId(),
                event.getEventType().name(),
                event.getActorUserId(),
                event.getTargetEntity(),
                event.getTargetId(),
                event.getIpAddress(),
                event.getCorrelationId(),
                event.getDescription(),
                event.getCreatedAt());
    }
}
