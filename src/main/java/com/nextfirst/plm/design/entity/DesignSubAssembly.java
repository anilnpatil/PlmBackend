package com.nextfirst.plm.design.entity;

import java.time.Instant;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "design_sub_assemblies",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_design_sub_assembly_main_number",
                columnNames = {"main_assembly_id", "assembly_number"}))
public class DesignSubAssembly {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "main_assembly_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private DesignMainAssembly mainAssembly;

    @Column(name = "assembly_number", nullable = false, length = 50)
    private String assemblyNumber;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(name = "created_by", nullable = false, length = 254)
    private String createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public DesignMainAssembly getMainAssembly() { return mainAssembly; }
    public void setMainAssembly(DesignMainAssembly mainAssembly) { this.mainAssembly = mainAssembly; }
    public String getAssemblyNumber() { return assemblyNumber; }
    public void setAssemblyNumber(String assemblyNumber) { this.assemblyNumber = assemblyNumber; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public Instant getCreatedAt() { return createdAt; }
}
