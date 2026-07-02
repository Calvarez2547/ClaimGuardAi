package com.claimguardai.audit;

import com.claimguardai.auth.AuthenticatedUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditEventRepository repo;

    public AuditController(AuditEventRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/events")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<Page<AuditEventResponse>> listEvents(
            @RequestParam(required = false) AuditEventType eventType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 200));
        Page<AuditEvent> events = repo.findFiltered(eventType, pageable);
        return ResponseEntity.ok(events.map(AuditEventResponse::from));
    }

    @GetMapping("/events/me")
    public ResponseEntity<Page<AuditEventResponse>> myEvents(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        Page<AuditEvent> events = repo.findByActorUserIdOrderByCreatedAtDesc(
                authenticatedUser.getId(), pageable);
        return ResponseEntity.ok(events.map(AuditEventResponse::from));
    }
}
