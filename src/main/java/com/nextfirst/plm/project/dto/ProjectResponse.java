package com.nextfirst.plm.project.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ProjectResponse(
        Long id,
        String projectNumber,
        String projectName,
        String projectDescription,
        String customerName,
        String customerAddress,
        LocalDate targetStartDate,
        LocalDate targetEndDate,
        LocalDateTime createdAt) {
}
