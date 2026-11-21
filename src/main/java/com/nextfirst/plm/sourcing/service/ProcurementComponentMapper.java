package com.nextfirst.plm.sourcing.service;

import com.nextfirst.plm.design.entity.DesignParts;
import com.nextfirst.plm.sourcing.dto.ProcurementComponentResponse;
import com.nextfirst.plm.sourcing.entity.ManufacturingComponentWorkflow;
import com.nextfirst.plm.sourcing.entity.PurchaseComponentWorkflow;

final class ProcurementComponentMapper {
    private ProcurementComponentMapper() {
    }

    static ProcurementComponentResponse from(DesignParts part, PurchaseComponentWorkflow workflow) {
        return new ProcurementComponentResponse(
                part.getId(),
                workflow == null ? null : workflow.getId(),
                part.getPartNumber(),
                part.getPartName(),
                workflow == null || workflow.getSupplier() == null ? null : workflow.getSupplier().getId(),
                workflow == null || workflow.getSupplier() == null ? null : workflow.getSupplier().getName(),
                part.getQuantity(),
                part.getMainAssembly().getAssemblyNumber(),
                part.getMainAssembly().getName(),
                part.getSubAssembly() == null ? null : part.getSubAssembly().getAssemblyNumber(),
                part.getSubAssembly() == null ? null : part.getSubAssembly().getName(),
                part.getDescription(),
                part.getFilePath(),
                workflow == null ? null : workflow.getQuotationStatus(),
                workflow == null ? null : workflow.getQuotationRequestedDate(),
                workflow == null ? null : workflow.getQuotationReceivedDate(),
                workflow == null ? null : workflow.getQuotationPrice(),
                workflow == null ? null : workflow.getNegotiationPrice(),
                workflow == null ? null : workflow.getFinalPrice(),
                workflow == null ? "INR" : workflow.getCurrencyCode(),
                workflow == null ? null : workflow.getPurchaseOrderStatus(),
                workflow == null ? null : workflow.getPurchaseOrderReleasedDate(),
                workflow == null ? null : workflow.getLeadTimeDays(),
                workflow == null ? null : workflow.getPartReceivingDate(),
                workflow == null ? null : workflow.getPartReceivedStatus(),
                workflow == null ? null : workflow.getQualityStatus());
    }

    static ProcurementComponentResponse from(DesignParts part, ManufacturingComponentWorkflow workflow) {
        return new ProcurementComponentResponse(
                part.getId(),
                workflow == null ? null : workflow.getId(),
                part.getPartNumber(),
                part.getPartName(),
                workflow == null || workflow.getSupplier() == null ? null : workflow.getSupplier().getId(),
                workflow == null || workflow.getSupplier() == null ? null : workflow.getSupplier().getName(),
                part.getQuantity(),
                part.getMainAssembly().getAssemblyNumber(),
                part.getMainAssembly().getName(),
                part.getSubAssembly() == null ? null : part.getSubAssembly().getAssemblyNumber(),
                part.getSubAssembly() == null ? null : part.getSubAssembly().getName(),
                part.getDescription(),
                part.getFilePath(),
                workflow == null ? null : workflow.getQuotationStatus(),
                workflow == null ? null : workflow.getQuotationRequestedDate(),
                workflow == null ? null : workflow.getQuotationReceivedDate(),
                workflow == null ? null : workflow.getQuotationPrice(),
                workflow == null ? null : workflow.getNegotiationPrice(),
                workflow == null ? null : workflow.getFinalPrice(),
                workflow == null ? "INR" : workflow.getCurrencyCode(),
                workflow == null ? null : workflow.getPurchaseOrderStatus(),
                workflow == null ? null : workflow.getPurchaseOrderReleasedDate(),
                workflow == null ? null : workflow.getLeadTimeDays(),
                workflow == null ? null : workflow.getPartReceivingDate(),
                workflow == null ? null : workflow.getPartReceivedStatus(),
                workflow == null ? null : workflow.getQualityStatus());
    }
}
