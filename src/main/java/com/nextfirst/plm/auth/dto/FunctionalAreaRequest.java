package com.nextfirst.plm.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record FunctionalAreaRequest(
        @NotBlank @Pattern(regexp = "[A-Za-z][A-Za-z0-9_]{1,79}") String code,
        @NotBlank @Size(max = 120) String name) {}
