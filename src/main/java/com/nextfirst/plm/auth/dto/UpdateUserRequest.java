package com.nextfirst.plm.auth.dto;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @NotBlank @Size(max = 120) String fullName,
        @Size(min = 12, max = 72) String password,
        @NotNull Set<@NotBlank String> roleCodes) {}
