package com.nextfirst.plm.sourcing.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.nextfirst.plm.design.entity.DesignParts;
import com.nextfirst.plm.supplier.entity.Supplier;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.PositiveOrZero;

@Entity
@Table(name = "manufacturing_component_workflows")
public class ManufacturingComponentWorkflow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "design_part_id", nullable = false, unique = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private DesignParts designPart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Enumerated(EnumType.STRING)
    @Column(name = "quotation_status", nullable = false, length = 20)
    private QuotationStatus quotationStatus = QuotationStatus.NOT_REQUESTED;

    private LocalDate quotationRequestedDate;
    private LocalDate quotationReceivedDate;

    @DecimalMin(value = "0.0", inclusive = true)
    @Column(name = "quotation_price", precision = 14, scale = 2)
    private BigDecimal quotationPrice;

    @DecimalMin(value = "0.0", inclusive = true)
    @Column(name = "negotiation_price", precision = 14, scale = 2)
    private BigDecimal negotiationPrice;

    @DecimalMin(value = "0.0", inclusive = true)
    @Column(name = "final_price", precision = 14, scale = 2)
    private BigDecimal finalPrice;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode = "INR";

    @Enumerated(EnumType.STRING)
    @Column(name = "purchase_order_status", nullable = false, length = 20)
    private PurchaseOrderStatus purchaseOrderStatus = PurchaseOrderStatus.NOT_RELEASED;

    private LocalDate purchaseOrderReleasedDate;

    @PositiveOrZero
    private Integer leadTimeDays;

    private LocalDate partReceivingDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "part_received_status", nullable = false, length = 20)
    private PartReceivedStatus partReceivedStatus = PartReceivedStatus.NOT_RECEIVED;

    @Enumerated(EnumType.STRING)
    @Column(name = "quality_status", nullable = false, length = 20)
    private QualityStatus qualityStatus = QualityStatus.PENDING;

    public Long getId() { return id; }
    public DesignParts getDesignPart() { return designPart; }
    public void setDesignPart(DesignParts designPart) { this.designPart = designPart; }
    public Supplier getSupplier() { return supplier; }
    public void setSupplier(Supplier supplier) { this.supplier = supplier; }
    public QuotationStatus getQuotationStatus() { return quotationStatus; }
    public void setQuotationStatus(QuotationStatus quotationStatus) { this.quotationStatus = quotationStatus; }
    public LocalDate getQuotationRequestedDate() { return quotationRequestedDate; }
    public void setQuotationRequestedDate(LocalDate quotationRequestedDate) { this.quotationRequestedDate = quotationRequestedDate; }
    public LocalDate getQuotationReceivedDate() { return quotationReceivedDate; }
    public void setQuotationReceivedDate(LocalDate quotationReceivedDate) { this.quotationReceivedDate = quotationReceivedDate; }
    public BigDecimal getQuotationPrice() { return quotationPrice; }
    public void setQuotationPrice(BigDecimal quotationPrice) { this.quotationPrice = quotationPrice; }
    public BigDecimal getNegotiationPrice() { return negotiationPrice; }
    public void setNegotiationPrice(BigDecimal negotiationPrice) { this.negotiationPrice = negotiationPrice; }
    public BigDecimal getFinalPrice() { return finalPrice; }
    public void setFinalPrice(BigDecimal finalPrice) { this.finalPrice = finalPrice; }
    public String getCurrencyCode() { return currencyCode; }
    public PurchaseOrderStatus getPurchaseOrderStatus() { return purchaseOrderStatus; }
    public void setPurchaseOrderStatus(PurchaseOrderStatus purchaseOrderStatus) { this.purchaseOrderStatus = purchaseOrderStatus; }
    public LocalDate getPurchaseOrderReleasedDate() { return purchaseOrderReleasedDate; }
    public void setPurchaseOrderReleasedDate(LocalDate purchaseOrderReleasedDate) { this.purchaseOrderReleasedDate = purchaseOrderReleasedDate; }
    public Integer getLeadTimeDays() { return leadTimeDays; }
    public void setLeadTimeDays(Integer leadTimeDays) { this.leadTimeDays = leadTimeDays; }
    public LocalDate getPartReceivingDate() { return partReceivingDate; }
    public void setPartReceivingDate(LocalDate partReceivingDate) { this.partReceivingDate = partReceivingDate; }
    public PartReceivedStatus getPartReceivedStatus() { return partReceivedStatus; }
    public void setPartReceivedStatus(PartReceivedStatus partReceivedStatus) { this.partReceivedStatus = partReceivedStatus; }
    public QualityStatus getQualityStatus() { return qualityStatus; }
    public void setQualityStatus(QualityStatus qualityStatus) { this.qualityStatus = qualityStatus; }
}
