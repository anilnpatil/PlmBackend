package com.nextfirst.plm.auth.dto;

import java.util.Set;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

public record ProjectAssignmentRequest(
        @NotNull Set<@NotBlank String> roleCodes,
        @NotNull Set<@NotBlank String> functionalAreaCodes) {}
