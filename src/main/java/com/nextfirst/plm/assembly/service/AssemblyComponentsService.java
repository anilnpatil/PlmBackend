package com.nextfirst.plm.assembly.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nextfirst.plm.assembly.dto.AssemblyComponentResponse;
import com.nextfirst.plm.assembly.entity.AssemblyComponentWorkflow;
import com.nextfirst.plm.assembly.entity.AssemblyStatus;
import com.nextfirst.plm.assembly.repository.AssemblyComponentWorkflowRepository;
import com.nextfirst.plm.auth.service.AuthAuditService;
import com.nextfirst.plm.design.entity.DesignParts;
import com.nextfirst.plm.design.entity.ModeOfPurchase;
import com.nextfirst.plm.design.entity.PartStatus;
import com.nextfirst.plm.design.exception.DesignResourceNotFoundException;
import com.nextfirst.plm.project.exception.ProjectNotFoundException;
import com.nextfirst.plm.project.repository.ProjectRepository;
import com.nextfirst.plm.sourcing.entity.AvailabilityStatus;
import com.nextfirst.plm.sourcing.entity.PartReceivedStatus;

@Service
public class AssemblyComponentsService {
    private final AssemblyComponentWorkflowRepository workflowRepository;
    private final ProjectRepository projectRepository;
    private final AuthAuditService auditService;

    public AssemblyComponentsService(
            AssemblyComponentWorkflowRepository workflowRepository,
            ProjectRepository projectRepository,
            AuthAuditService auditService) {
        this.workflowRepository = workflowRepository;
        this.projectRepository = projectRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@authorizationService.hasProjectPermission(authentication, #p0, 'ASSEMBLY_READ', 'ASSEMBLY')")
    public List<AssemblyComponentResponse> components(
            @P("projectId") Long projectId, Authentication authentication) {
        requireProject(projectId);
        return workflowRepository.findEligibleComponents(
                projectId,
                ModeOfPurchase.PURCHASE,
                ModeOfPurchase.MANUFACTURE,
                ModeOfPurchase.STOCK,
                PartReceivedStatus.RECEIVED,
                PartStatus.READY_TO_PURCHASE,
                AvailabilityStatus.AVAILABLE);
    }

    @Transactional
    @PreAuthorize("@authorizationService.hasProjectPermission(authentication, #p0, 'ASSEMBLY_PROGRESS_UPDATE', 'ASSEMBLY')")
    public AssemblyComponentResponse start(
            @P("projectId") Long projectId,
            Long designPartId,
            Authentication authentication) {
        DesignParts part = eligiblePart(projectId, designPartId);
        AssemblyComponentWorkflow workflow = workflowRepository.findByDesignPartId(designPartId)
                .orElseGet(AssemblyComponentWorkflow::new);
        if (workflow.getAssemblyStatus() != AssemblyStatus.NOT_STARTED
                || workflow.getAssemblyStartDate() != null
                || workflow.getAssemblyCompleteDate() != null) {
            throw new IllegalArgumentException("Assembly work has already been started.");
        }

        workflow.setDesignPart(part);
        workflow.setAssemblyStartDate(LocalDate.now());
        workflow.setAssemblyStatus(AssemblyStatus.IN_PROGRESS);
        workflow = workflowRepository.save(workflow);

        auditService.record(
                "ASSEMBLY_WORKFLOW_STARTED",
                "ASSEMBLY_COMPONENT_WORKFLOW",
                workflow.getId(),
                part.getPartNumber());
        return toResponse(part, workflow);
    }

    @Transactional
    @PreAuthorize("@authorizationService.hasProjectPermission(authentication, #p0, 'ASSEMBLY_PROGRESS_UPDATE', 'ASSEMBLY')")
    public AssemblyComponentResponse complete(
            @P("projectId") Long projectId,
            Long designPartId,
            Authentication authentication) {
        DesignParts part = eligiblePart(projectId, designPartId);
        AssemblyComponentWorkflow workflow = workflowRepository.findByDesignPartId(designPartId)
                .orElseThrow(() -> new IllegalArgumentException("Start assembly before completing it."));
        if (workflow.getAssemblyStatus() != AssemblyStatus.IN_PROGRESS
                || workflow.getAssemblyStartDate() == null
                || workflow.getAssemblyCompleteDate() != null) {
            throw new IllegalArgumentException("Only in-progress assembly work can be completed.");
        }

        LocalDate completedToday = LocalDate.now();
        if (completedToday.isBefore(workflow.getAssemblyStartDate())) {
            throw new IllegalArgumentException("Assembly completion date cannot be before its start date.");
        }
        workflow.setAssemblyCompleteDate(completedToday);
        workflow.setAssemblyStatus(AssemblyStatus.COMPLETED);
        workflow = workflowRepository.save(workflow);

        auditService.record(
                "ASSEMBLY_WORKFLOW_COMPLETED",
                "ASSEMBLY_COMPONENT_WORKFLOW",
                workflow.getId(),
                part.getPartNumber());
        return toResponse(part, workflow);
    }

    private AssemblyComponentResponse toResponse(DesignParts part, AssemblyComponentWorkflow workflow) {
        return new AssemblyComponentResponse(
                part.getId(),
                part.getMainAssembly().getId(),
                part.getMainAssembly().getAssemblyNumber(),
                part.getMainAssembly().getName(),
                part.getSubAssembly() == null ? null : part.getSubAssembly().getId(),
                part.getSubAssembly() == null ? null : part.getSubAssembly().getAssemblyNumber(),
                part.getSubAssembly() == null ? null : part.getSubAssembly().getName(),
                part.getPartNumber(),
                part.getPartName(),
                part.getFilePath(),
                part.getQuantity(),
                part.getModeOfPurchase(),
                workflow.getAssemblyStartDate(),
                workflow.getAssemblyCompleteDate(),
                workflow.getAssemblyStatus());
    }

    private DesignParts eligiblePart(Long projectId, Long designPartId) {
        requireProject(projectId);
        return workflowRepository.findEligiblePart(
                        projectId,
                        designPartId,
                        ModeOfPurchase.PURCHASE,
                        ModeOfPurchase.MANUFACTURE,
                        ModeOfPurchase.STOCK,
                        PartReceivedStatus.RECEIVED,
                        PartStatus.READY_TO_PURCHASE,
                        AvailabilityStatus.AVAILABLE)
                .orElseThrow(() -> new DesignResourceNotFoundException("Assembly component", designPartId));
    }

    private void requireProject(Long projectId) {
        projectRepository.findById(projectId).orElseThrow(() -> new ProjectNotFoundException(projectId));
    }
}
