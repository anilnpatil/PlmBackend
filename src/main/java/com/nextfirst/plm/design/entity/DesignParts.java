package com.nextfirst.plm.design.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "design_register_parts",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_design_part_main_number",
                        columnNames = {"main_assembly_id", "part_number"}),
                @UniqueConstraint(
                        name = "uk_design_part_sub_number",
                        columnNames = {"sub_assembly_id", "part_number"})
        })
public class DesignParts {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "main_assembly_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private DesignMainAssembly mainAssembly;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_assembly_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private DesignSubAssembly subAssembly;

    @Column(name = "part_number", nullable = false, length = 50)
    private String partNumber;

    @Column(name = "part_name", nullable = false, length = 120)
    private String partName;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantity;

    @Column(length = 2000)
    private String description;

    @Column(length = 100)
    private String make;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode_of_purchase", length = 20)
    private ModeOfPurchase modeOfPurchase;

    @Enumerated(EnumType.STRING)
    @Column(name = "part_status", length = 30)
    private PartStatus partStatus;

    @Column(name = "created_by", nullable = false, length = 254)
    private String createdBy;

    @Column(name = "file_path", length = 2048)
    private String filePath;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "completed_date")
    private LocalDate completedDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        if (modeOfPurchase == null) {
            modeOfPurchase = ModeOfPurchase.PURCHASE;
        }
        if (partStatus == null) {
            partStatus = PartStatus.NOT_STARTED;
        }
    }

    public Long getId() { return id; }
    public DesignMainAssembly getMainAssembly() { return mainAssembly; }
    public void setMainAssembly(DesignMainAssembly mainAssembly) { this.mainAssembly = mainAssembly; }
    public DesignSubAssembly getSubAssembly() { return subAssembly; }
    public void setSubAssembly(DesignSubAssembly subAssembly) { this.subAssembly = subAssembly; }
    public String getPartNumber() { return partNumber; }
    public void setPartNumber(String partNumber) { this.partNumber = partNumber; }
    public String getPartName() { return partName; }
    public void setPartName(String partName) { this.partName = partName; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getMake() { return make; }
    public void setMake(String make) { this.make = make; }
    public ModeOfPurchase getModeOfPurchase() { return modeOfPurchase; }
    public void setModeOfPurchase(ModeOfPurchase modeOfPurchase) { this.modeOfPurchase = modeOfPurchase; }
    public PartStatus getPartStatus() { return partStatus; }
    public void setPartStatus(PartStatus partStatus) { this.partStatus = partStatus; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getCompletedDate() { return completedDate; }
    public void setCompletedDate(LocalDate completedDate) { this.completedDate = completedDate; }
    public Instant getCreatedAt() { return createdAt; }
}
