package com.nextfirst.plm.sourcing.dto;

import java.math.BigDecimal;

import com.nextfirst.plm.sourcing.entity.AvailabilityStatus;

public record ProjectStockComponentResponse(
        Long designPartId,
        Long stockItemId,
        String partNumber,
        String partName,
        BigDecimal quantity,
        String mainAssemblyNumber,
        String subAssemblyNumber,
        String description,
        String filePath,
        BigDecimal totalWeightKg,
        AvailabilityStatus availabilityStatus) {
}
