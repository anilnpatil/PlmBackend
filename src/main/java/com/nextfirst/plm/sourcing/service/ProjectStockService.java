package com.nextfirst.plm.sourcing.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
import com.nextfirst.plm.sourcing.dto.ProjectStockComponentResponse;
import com.nextfirst.plm.sourcing.dto.ProjectStockUpdateRequest;
import com.nextfirst.plm.sourcing.entity.AvailabilityStatus;
import com.nextfirst.plm.sourcing.entity.ProjectStockItem;
import com.nextfirst.plm.sourcing.repository.ProjectStockItemRepository;

@Service
public class ProjectStockService {
    private final DesignPartsRepository designPartsRepository;
    private final ProjectStockItemRepository stockRepository;
    private final ProjectRepository projectRepository;
    private final AuthAuditService auditService;

    public ProjectStockService(
            DesignPartsRepository designPartsRepository,
            ProjectStockItemRepository stockRepository,
            ProjectRepository projectRepository,
            AuthAuditService auditService) {
        this.designPartsRepository = designPartsRepository;
        this.stockRepository = stockRepository;
        this.projectRepository = projectRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@authorizationService.hasProjectPermission(authentication, #p0, 'PROJECT_STOCK_READ', 'STOCK')")
    public List<ProjectStockComponentResponse> components(
            @P("projectId") Long projectId, Authentication authentication) {
        requireProject(projectId);
        List<DesignParts> parts = eligibleParts(projectId);
        Map<Long, ProjectStockItem> stockItems = stockRepository.findByDesignPartIdIn(
                        parts.stream().map(DesignParts::getId).toList())
                .stream().collect(Collectors.toMap(
                        item -> item.getDesignPart().getId(), Function.identity()));
        return parts.stream().map(part -> toResponse(part, stockItems.get(part.getId()))).toList();
    }

    @Transactional
    @PreAuthorize("@authorizationService.hasProjectPermission(authentication, #p0, 'PROJECT_STOCK_UPDATE', 'STOCK')")
    public ProjectStockComponentResponse update(
            @P("projectId") Long projectId,
            Long designPartId,
            ProjectStockUpdateRequest request,
            Authentication authentication) {
        DesignParts part = requireEligiblePart(projectId, designPartId);
        ProjectStockItem stockItem = stockRepository.findByDesignPartId(designPartId)
                .orElseGet(ProjectStockItem::new);
        boolean created = stockItem.getId() == null;
        stockItem.setDesignPart(part);
        stockItem.setTotalWeightKg(request.totalWeightKg());
        stockItem.setAvailabilityStatus(request.availabilityStatus());
        stockItem = stockRepository.save(stockItem);
        auditService.record(created ? "PROJECT_STOCK_ITEM_CREATED" : "PROJECT_STOCK_ITEM_UPDATED",
                "PROJECT_STOCK_ITEM", stockItem.getId(), part.getPartNumber());
        return toResponse(part, stockItem);
    }

    private List<DesignParts> eligibleParts(Long projectId) {
        return designPartsRepository.findReadyForMode(
                projectId, ModeOfPurchase.STOCK, PartStatus.READY_TO_PURCHASE, false);
    }

    private DesignParts requireEligiblePart(Long projectId, Long designPartId) {
        requireProject(projectId);
        DesignParts part = designPartsRepository.findByIdAndMainAssemblyProjectId(designPartId, projectId)
                .orElseThrow(() -> new DesignResourceNotFoundException("Part", designPartId));
        if (part.getModeOfPurchase() != ModeOfPurchase.STOCK
                || part.getPartStatus() != PartStatus.READY_TO_PURCHASE) {
            throw new IllegalArgumentException(
                    "Only parts marked ready and assigned to project stock can be updated here");
        }
        return part;
    }

    private ProjectStockComponentResponse toResponse(DesignParts part, ProjectStockItem stockItem) {
        return new ProjectStockComponentResponse(
                part.getId(),
                stockItem == null ? null : stockItem.getId(),
                part.getPartNumber(),
                part.getPartName(),
                part.getQuantity(),
                part.getMainAssembly().getAssemblyNumber(),
                part.getSubAssembly() == null ? null : part.getSubAssembly().getAssemblyNumber(),
                part.getDescription(),
                part.getFilePath(),
                stockItem == null ? null : stockItem.getTotalWeightKg(),
                stockItem == null ? AvailabilityStatus.AVAILABLE : stockItem.getAvailabilityStatus());
    }

    private void requireProject(Long projectId) {
        projectRepository.findById(projectId).orElseThrow(() -> new ProjectNotFoundException(projectId));
    }
}
