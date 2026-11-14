package com.nextfirst.plm.sourcing.dto;

import java.math.BigDecimal;

import com.nextfirst.plm.sourcing.entity.AvailabilityStatus;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record ProjectStockUpdateRequest(
        @NotNull @DecimalMin(value = "0.0", inclusive = true) BigDecimal totalWeightKg,
        @NotNull AvailabilityStatus availabilityStatus) {
}
