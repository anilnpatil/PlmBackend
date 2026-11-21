package com.nextfirst.plm.assembly.entity;

import java.time.LocalDate;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.nextfirst.plm.design.entity.DesignParts;

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

@Entity
@Table(name = "assembly_component_workflows")
public class AssemblyComponentWorkflow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "design_part_id", nullable = false, unique = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private DesignParts designPart;

    @Column(name = "assembly_start_date")
    private LocalDate assemblyStartDate;

    @Column(name = "assembly_complete_date")
    private LocalDate assemblyCompleteDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "assembly_status", nullable = false, length = 20)
    private AssemblyStatus assemblyStatus = AssemblyStatus.NOT_STARTED;

    public Long getId() { return id; }
    public DesignParts getDesignPart() { return designPart; }
    public void setDesignPart(DesignParts designPart) { this.designPart = designPart; }
    public LocalDate getAssemblyStartDate() { return assemblyStartDate; }
    public void setAssemblyStartDate(LocalDate assemblyStartDate) { this.assemblyStartDate = assemblyStartDate; }
    public LocalDate getAssemblyCompleteDate() { return assemblyCompleteDate; }
    public void setAssemblyCompleteDate(LocalDate assemblyCompleteDate) { this.assemblyCompleteDate = assemblyCompleteDate; }
    public AssemblyStatus getAssemblyStatus() { return assemblyStatus; }
    public void setAssemblyStatus(AssemblyStatus assemblyStatus) { this.assemblyStatus = assemblyStatus; }
}
