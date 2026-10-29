package com.nextfirst.plm.auth.dto;

import java.time.Instant;

public record AuditEventResponse(
        Long id, String actorEmail, String action, String targetType,
        String targetId, String details, Instant createdAt) {}
