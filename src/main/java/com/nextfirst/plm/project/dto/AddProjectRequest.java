package com.nextfirst.plm.project.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddProjectRequest(
        @NotBlank @Size(max = 30) String projectNumber,
        @NotBlank @Size(max = 30) String projectName,
        @Size(max = 2000) String projectDescription,
        @NotBlank @Size(max = 30) String customerName,
        @Size(max = 100) String customerAddress,
        LocalDate targetStartDate,
        LocalDate targetEndDate) {

    @AssertTrue(message = "Target end date must not be before target start date")
    public boolean isTargetDateRangeValid() {
        return targetStartDate == null || targetEndDate == null || !targetEndDate.isBefore(targetStartDate);
    }
}
