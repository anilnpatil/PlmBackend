package com.nextfirst.plm.sourcing.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.nextfirst.plm.sourcing.entity.PartReceivedStatus;
import com.nextfirst.plm.sourcing.entity.PurchaseOrderStatus;
import com.nextfirst.plm.sourcing.entity.QualityStatus;
import com.nextfirst.plm.sourcing.entity.QuotationStatus;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ProcurementWorkflowRequest(
        @Size(max = 150) String supplierName,
        @NotNull QuotationStatus quotationStatus,
        LocalDate quotationRequestedDate,
        LocalDate quotationReceivedDate,
        @DecimalMin(value = "0.0", inclusive = true) BigDecimal quotationPrice,
        @DecimalMin(value = "0.0", inclusive = true) BigDecimal negotiationPrice,
        @DecimalMin(value = "0.0", inclusive = true) BigDecimal finalPrice,
        @NotNull PurchaseOrderStatus purchaseOrderStatus,
        LocalDate purchaseOrderReleasedDate,
        @PositiveOrZero Integer leadTimeDays,
        LocalDate partReceivingDate,
        @NotNull PartReceivedStatus partReceivedStatus,
        @NotNull QualityStatus qualityStatus) {
}
