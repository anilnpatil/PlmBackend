package com.nextfirst.plm.sourcing.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.nextfirst.plm.sourcing.entity.PartReceivedStatus;
import com.nextfirst.plm.sourcing.entity.PurchaseOrderStatus;
import com.nextfirst.plm.sourcing.entity.QualityStatus;
import com.nextfirst.plm.sourcing.entity.QuotationStatus;

public record ProcurementComponentResponse(
        Long designPartId,
        Long workflowId,
        String partNumber,
        String partName,
        Long supplierId,
        String supplierName,
        BigDecimal quantity,
        String mainAssemblyNumber,
        String mainAssemblyName,
        String subAssemblyNumber,
        String subAssemblyName,
        String description,
        String filePath,
        QuotationStatus quotationStatus,
        LocalDate quotationRequestedDate,
        LocalDate quotationReceivedDate,
        BigDecimal quotationPrice,
        BigDecimal negotiationPrice,
        BigDecimal finalPrice,
        String currencyCode,
        PurchaseOrderStatus purchaseOrderStatus,
        LocalDate purchaseOrderReleasedDate,
        Integer leadTimeDays,
        LocalDate partReceivingDate,
        PartReceivedStatus partReceivedStatus,
        QualityStatus qualityStatus) {
}
