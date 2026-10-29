package com.nextfirst.plm.auth.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nextfirst.plm.auth.dto.AuditEventResponse;
import com.nextfirst.plm.auth.repository.AuthAuditEventRepository;

@Service
public class AuthAuditQueryService {
    private final AuthAuditEventRepository auditRepository;

    public AuthAuditQueryService(AuthAuditEventRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@authorizationService.hasGlobalPermission(authentication, 'AUDIT_READ')")
    public List<AuditEventResponse> latestEvents() {
        return auditRepository.findAllByOrderByCreatedAtDesc(
                        PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(event -> new AuditEventResponse(
                        event.getId(),
                        event.getActorEmail(),
                        event.getAction(),
                        event.getTargetType(),
                        event.getTargetId(),
                        event.getDetails(),
                        event.getCreatedAt()))
                .getContent();
    }
}
