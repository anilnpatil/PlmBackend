package com.nextfirst.plm.sourcing.service;

import com.nextfirst.plm.sourcing.dto.ProcurementWorkflowRequest;
import com.nextfirst.plm.sourcing.entity.PartReceivedStatus;
import com.nextfirst.plm.sourcing.entity.PurchaseOrderStatus;
import com.nextfirst.plm.sourcing.entity.QualityStatus;
import com.nextfirst.plm.sourcing.entity.QuotationStatus;

final class ProcurementWorkflowValidator {
    private ProcurementWorkflowValidator() {
    }

    static void validate(ProcurementWorkflowRequest request) {
        if (request.quotationStatus() != QuotationStatus.NOT_REQUESTED
                && request.quotationRequestedDate() == null) {
            throw new IllegalArgumentException("A quotation request date is required once a quotation is requested");
        }
        if (request.quotationStatus() == QuotationStatus.RECEIVED
                && request.quotationReceivedDate() == null) {
            throw new IllegalArgumentException("A quotation received date is required when the quotation is received");
        }
        if (request.quotationRequestedDate() != null
                && request.quotationReceivedDate() != null
                && request.quotationReceivedDate().isBefore(request.quotationRequestedDate())) {
            throw new IllegalArgumentException("Quotation received date cannot be before its request date");
        }
        if (request.purchaseOrderStatus() == PurchaseOrderStatus.RELEASED
                && request.purchaseOrderReleasedDate() == null) {
            throw new IllegalArgumentException("A purchase order release date is required when the PO is released");
        }
        if (request.partReceivedStatus() != PartReceivedStatus.NOT_RECEIVED
                && request.partReceivingDate() == null) {
            throw new IllegalArgumentException("A part receiving date is required once parts are received");
        }
        if (request.qualityStatus() != QualityStatus.PENDING
                && request.partReceivedStatus() == PartReceivedStatus.NOT_RECEIVED) {
            throw new IllegalArgumentException("Quality can only be recorded after parts are received");
        }
    }
}
