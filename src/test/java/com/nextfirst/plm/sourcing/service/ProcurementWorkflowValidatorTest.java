package com.nextfirst.plm.sourcing.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.nextfirst.plm.sourcing.dto.ProcurementWorkflowRequest;
import com.nextfirst.plm.sourcing.entity.PartReceivedStatus;
import com.nextfirst.plm.sourcing.entity.PurchaseOrderStatus;
import com.nextfirst.plm.sourcing.entity.QualityStatus;
import com.nextfirst.plm.sourcing.entity.QuotationStatus;

class ProcurementWorkflowValidatorTest {
    @Test
    void requiresDatesForRequestedAndReceivedQuotations() {
        ProcurementWorkflowRequest request = request(
                QuotationStatus.REQUESTED, null, null,
                PurchaseOrderStatus.NOT_RELEASED, null,
                PartReceivedStatus.NOT_RECEIVED, null, QualityStatus.PENDING);

        assertThrows(IllegalArgumentException.class, () -> ProcurementWorkflowValidator.validate(request));
    }

    @Test
    void requiresReleaseAndReceivingDatesWhenTheirStatusesAdvance() {
        ProcurementWorkflowRequest missingReleaseDate = request(
                QuotationStatus.NOT_REQUESTED, null, null,
                PurchaseOrderStatus.RELEASED, null,
                PartReceivedStatus.NOT_RECEIVED, null, QualityStatus.PENDING);
        ProcurementWorkflowRequest missingReceivingDate = request(
                QuotationStatus.NOT_REQUESTED, null, null,
                PurchaseOrderStatus.NOT_RELEASED, null,
                PartReceivedStatus.RECEIVED, null, QualityStatus.OK);

        assertThrows(IllegalArgumentException.class,
                () -> ProcurementWorkflowValidator.validate(missingReleaseDate));
        assertThrows(IllegalArgumentException.class,
                () -> ProcurementWorkflowValidator.validate(missingReceivingDate));
    }

    @Test
    void rejectsQualityBeforeReceiptAndAcceptsACompletedWorkflow() {
        ProcurementWorkflowRequest qualityBeforeReceipt = request(
                QuotationStatus.NOT_REQUESTED, null, null,
                PurchaseOrderStatus.NOT_RELEASED, null,
                PartReceivedStatus.NOT_RECEIVED, null, QualityStatus.REWORK);
        ProcurementWorkflowRequest completed = request(
                QuotationStatus.RECEIVED, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 3),
                PurchaseOrderStatus.RELEASED, LocalDate.of(2026, 10, 5),
                PartReceivedStatus.RECEIVED, LocalDate.of(2026, 10, 12), QualityStatus.OK);

        assertThrows(IllegalArgumentException.class,
                () -> ProcurementWorkflowValidator.validate(qualityBeforeReceipt));
        assertDoesNotThrow(() -> ProcurementWorkflowValidator.validate(completed));
    }

    private ProcurementWorkflowRequest request(
            QuotationStatus quotationStatus,
            LocalDate quotationRequestedDate,
            LocalDate quotationReceivedDate,
            PurchaseOrderStatus purchaseOrderStatus,
            LocalDate purchaseOrderReleasedDate,
            PartReceivedStatus partReceivedStatus,
            LocalDate partReceivingDate,
            QualityStatus qualityStatus) {
        return new ProcurementWorkflowRequest(
                null,
                quotationStatus,
                quotationRequestedDate,
                quotationReceivedDate,
                null,
                null,
                null,
                purchaseOrderStatus,
                purchaseOrderReleasedDate,
                null,
                partReceivingDate,
                partReceivedStatus,
                qualityStatus);
    }
}
