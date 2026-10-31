package com.nextfirst.plm.design.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import com.nextfirst.plm.design.entity.ModeOfPurchase;
import com.nextfirst.plm.design.entity.PartStatus;

public record DesignPartsResponse(
        Long id,
        Long mainAssemblyId,
        Long subAssemblyId,
        String partNumber,
        String partName,
        BigDecimal quantity,
        String description,
        String make,
        ModeOfPurchase modeOfPurchase,
        PartStatus partStatus,
        String createdBy,
        String filePath,
        LocalDate startDate,
        LocalDate completedDate,
        Instant createdAt) {
}
