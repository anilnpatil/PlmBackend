package com.nextfirst.plm.auth.dto;

import java.time.Instant;
import java.util.Set;

public record ProjectAssignmentResponse(
        Long userId, String email, String fullName, Set<String> roles, Set<String> functionalAreas, Instant assignedAt) {}
