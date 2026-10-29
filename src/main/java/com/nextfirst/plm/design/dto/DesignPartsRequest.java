package com.nextfirst.plm.design.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DesignPartsRequest(
        Long mainAssemblyId,
        Long subAssemblyId,
        @NotBlank @Size(max = 50) String partNumber,
        @NotBlank @Size(max = 120) String partName,
        @NotNull @DecimalMin(value = "0.001") BigDecimal quantity,
        @Size(max = 2000) String description,
        @Size(max = 100) String make,
        @Size(max = 2048) String filePath,
        LocalDate startDate,
        LocalDate completedDate) {
}
