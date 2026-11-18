package com.nextfirst.plm.sourcing.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nextfirst.plm.auth.service.AuthAuditService;
import com.nextfirst.plm.design.entity.DesignParts;
import com.nextfirst.plm.design.entity.ModeOfPurchase;
import com.nextfirst.plm.design.entity.PartStatus;
import com.nextfirst.plm.design.exception.DesignResourceNotFoundException;
import com.nextfirst.plm.design.repository.DesignPartsRepository;
import com.nextfirst.plm.project.exception.ProjectNotFoundException;
import com.nextfirst.plm.project.repository.ProjectRepository;
import com.nextfirst.plm.sourcing.dto.ProcurementComponentResponse;
import com.nextfirst.plm.sourcing.dto.ProcurementWorkflowRequest;
import com.nextfirst.plm.sourcing.entity.ManufacturingComponentWorkflow;
import com.nextfirst.plm.sourcing.repository.ManufacturingComponentWorkflowRepository;
import com.nextfirst.plm.supplier.entity.SupplierServiceType;

@Service
public class ManufacturingComponentsService {
    private final DesignPartsRepository designPartsRepository;
    private final ManufacturingComponentWorkflowRepository workflowRepository;
    private final ProjectRepository projectRepository;
    private final AuthAuditService auditService;
    private final SupplierSelectionResolver supplierSelectionResolver;

    public ManufacturingComponentsService(
            DesignPartsRepository designPartsRepository,
            ManufacturingComponentWorkflowRepository workflowRepository,
            ProjectRepository projectRepository,
            AuthAuditService auditService,
            SupplierSelectionResolver supplierSelectionResolver) {
        this.designPartsRepository = designPartsRepository;
        this.workflowRepository = workflowRepository;
        this.projectRepository = projectRepository;
        this.auditService = auditService;
        this.supplierSelectionResolver = supplierSelectionResolver;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@authorizationService.hasProjectPermission(authentication, #p0, 'MANUFACTURING_COMPONENTS_READ', 'MANUFACTURING')")
    public List<ProcurementComponentResponse> components(
            @P("projectId") Long projectId, Authentication authentication) {
        requireProject(projectId);
        List<DesignParts> parts = eligibleParts(projectId);
        Map<Long, ManufacturingComponentWorkflow> workflows = workflowRepository.findByDesignPartIdIn(
                        parts.stream().map(DesignParts::getId).toList())
                .stream().collect(Collectors.toMap(
                        workflow -> workflow.getDesignPart().getId(), Function.identity()));
        return parts.stream()
                .map(part -> ProcurementComponentMapper.from(part, workflows.get(part.getId())))
                .toList();
    }

    @Transactional
    @PreAuthorize("@authorizationService.hasProjectPermission(authentication, #p0, 'MANUFACTURING_COMPONENTS_UPDATE', 'MANUFACTURING')")
    public ProcurementComponentResponse update(
            @P("projectId") Long projectId,
            Long designPartId,
            ProcurementWorkflowRequest request,
            Authentication authentication) {
        ProcurementWorkflowValidator.validate(request);
        DesignParts part = requireEligiblePart(projectId, designPartId);
        ManufacturingComponentWorkflow workflow = workflowRepository.findByDesignPartId(designPartId)
                .orElseGet(ManufacturingComponentWorkflow::new);
        boolean created = workflow.getId() == null;
        workflow.setDesignPart(part);
        workflow.setSupplier(supplierSelectionResolver.resolve(
                request.supplierId(), request.supplierName(), SupplierServiceType.MANUFACTURING));
        workflow.setQuotationStatus(request.quotationStatus());
        workflow.setQuotationRequestedDate(request.quotationRequestedDate());
        workflow.setQuotationReceivedDate(request.quotationReceivedDate());
        workflow.setQuotationPrice(request.quotationPrice());
        workflow.setNegotiationPrice(request.negotiationPrice());
        workflow.setFinalPrice(request.finalPrice());
        workflow.setPurchaseOrderStatus(request.purchaseOrderStatus());
        workflow.setPurchaseOrderReleasedDate(request.purchaseOrderReleasedDate());
        workflow.setLeadTimeDays(request.leadTimeDays());
        workflow.setPartReceivingDate(request.partReceivingDate());
        workflow.setPartReceivedStatus(request.partReceivedStatus());
        workflow.setQualityStatus(request.qualityStatus());
        workflow = workflowRepository.save(workflow);
        auditService.record(created ? "MANUFACTURING_WORKFLOW_CREATED" : "MANUFACTURING_WORKFLOW_UPDATED",
                "MANUFACTURING_COMPONENT_WORKFLOW", workflow.getId(), part.getPartNumber());
        return ProcurementComponentMapper.from(part, workflow);
    }

    private List<DesignParts> eligibleParts(Long projectId) {
        return designPartsRepository.findReadyForMode(
                projectId, ModeOfPurchase.MANUFACTURE, PartStatus.READY_TO_PURCHASE, false);
    }

    private DesignParts requireEligiblePart(Long projectId, Long designPartId) {
        requireProject(projectId);
        DesignParts part = designPartsRepository.findByIdAndMainAssemblyProjectId(designPartId, projectId)
                .orElseThrow(() -> new DesignResourceNotFoundException("Part", designPartId));
        if (part.getModeOfPurchase() != ModeOfPurchase.MANUFACTURE
                || part.getPartStatus() != PartStatus.READY_TO_PURCHASE) {
            throw new IllegalArgumentException(
                    "Only parts ready for manufacture can be updated in the Manufacturing workspace");
        }
        return part;
    }

    private void requireProject(Long projectId) {
        projectRepository.findById(projectId).orElseThrow(() -> new ProjectNotFoundException(projectId));
    }

}
