package com.nextfirst.plm.design.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record DesignPartsResponse(
        Long id,
        Long mainAssemblyId,
        Long subAssemblyId,
        String partNumber,
        String partName,
        BigDecimal quantity,
        String description,
        String make,
        String createdBy,
        String filePath,
        LocalDate startDate,
        LocalDate completedDate,
        Instant createdAt) {
}
