package com.nextfirst.plm.auth.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nextfirst.plm.auth.dto.AuditEventResponse;
import com.nextfirst.plm.auth.service.AuthAuditQueryService;

@RestController
@RequestMapping("/api/admin/audit-events")
public class AuthAuditController {
    private final AuthAuditQueryService queryService;

    public AuthAuditController(AuthAuditQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping
    @PreAuthorize("@authorizationService.hasGlobalPermission(authentication, 'AUDIT_READ')")
    public List<AuditEventResponse> latestEvents() {
        return queryService.latestEvents();
    }
}
