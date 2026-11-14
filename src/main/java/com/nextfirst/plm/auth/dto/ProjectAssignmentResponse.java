package com.nextfirst.plm.auth.dto;

import java.time.Instant;
import java.util.Set;
import java.util.Map;

public record ProjectAssignmentResponse(
        Long userId,
        String email,
        String fullName,
        Set<String> roles,
        Set<String> functionalAreas,
        Map<String, String> areaAccess,
        Instant assignedAt) {}
