package com.nextfirst.plm.design.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DesignMainAssemblyRequest(
        @NotBlank @Size(max = 50) String assemblyNumber,
        @NotBlank @Size(max = 120) String name,
        @Size(max = 2000) String description) {
}
