package com.nextfirst.plm.auth.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.nextfirst.plm.auth.entity.AuthAuditEvent;
import com.nextfirst.plm.auth.repository.AuthAuditEventRepository;

@Service
public class AuthAuditService {
    private final AuthAuditEventRepository auditRepository;

    public AuthAuditService(AuthAuditEventRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    public void record(String action, String targetType, Object targetId, String details) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String actor = authentication == null || !authentication.isAuthenticated()
                ? "system"
                : authentication.getName();
        auditRepository.save(new AuthAuditEvent(
                actor, action, targetType, targetId == null ? null : targetId.toString(), details));
    }
}
