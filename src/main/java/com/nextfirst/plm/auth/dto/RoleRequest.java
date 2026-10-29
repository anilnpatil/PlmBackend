package com.nextfirst.plm.auth.dto;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RoleRequest(
        @NotBlank @Pattern(regexp = "[A-Z][A-Z0-9_]{1,79}") String code,
        @NotBlank @Size(max = 120) String name,
        @Size(max = 500) String description,
        @NotNull Set<@NotBlank @Pattern(regexp = "[A-Z][A-Z0-9_]{1,99}") String> permissionCodes) {}
