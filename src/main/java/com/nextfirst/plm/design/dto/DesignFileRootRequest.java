package com.nextfirst.plm.design.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DesignFileRootRequest(
        @NotBlank @Size(max = 2048) String path) {
}
