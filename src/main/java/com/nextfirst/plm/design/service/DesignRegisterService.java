package com.nextfirst.plm.design.service;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nextfirst.plm.auth.service.AuthAuditService;
import com.nextfirst.plm.auth.service.AuthorizationService;
import com.nextfirst.plm.design.dto.DesignMainAssemblyRequest;
import com.nextfirst.plm.design.dto.DesignMainAssemblyResponse;
import com.nextfirst.plm.design.dto.DesignPartStatusRequest;
import com.nextfirst.plm.design.dto.DesignPartsRequest;
import com.nextfirst.plm.design.dto.DesignPartsResponse;
import com.nextfirst.plm.design.dto.DesignPermissions;
import com.nextfirst.plm.design.dto.DesignSubAssemblyRequest;
import com.nextfirst.plm.design.dto.DesignSubAssemblyResponse;
import com.nextfirst.plm.design.dto.DesignWorkspaceResponse;
import com.nextfirst.plm.design.entity.DesignMainAssembly;
import com.nextfirst.plm.design.entity.DesignParts;
import com.nextfirst.plm.design.entity.DesignSubAssembly;
import com.nextfirst.plm.design.entity.ModeOfPurchase;
import com.nextfirst.plm.design.entity.PartStatus;
import com.nextfirst.plm.design.exception.DesignResourceNotFoundException;
import com.nextfirst.plm.design.repository.DesignMainAssemblyRepository;
import com.nextfirst.plm.design.repository.DesignPartsRepository;
import com.nextfirst.plm.design.repository.DesignSubAssemblyRepository;
import com.nextfirst.plm.project.entity.Projects;
import com.nextfirst.plm.project.exception.ProjectNotFoundException;
import com.nextfirst.plm.project.repository.ProjectRepository;

@Service
public class DesignRegisterService {
    private static final String DESIGN_AREA = "DESIGN";

    private final DesignMainAssemblyRepository mainAssemblyRepository;
    private final DesignSubAssemblyRepository subAssemblyRepository;
    private final DesignPartsRepository partsRepository;
    private final ProjectRepository projectRepository;
    private final AuthorizationService authorizationService;
    private final AuthAuditService auditService;

    public DesignRegisterService(
            DesignMainAssemblyRepository mainAssemblyRepository,
            DesignSubAssemblyRepository subAssemblyRepository,
            DesignPartsRepository partsRepository,
            ProjectRepository projectRepository,
            AuthorizationService authorizationService,
            AuthAuditService auditService) {
        this.mainAssemblyRepository = mainAssemblyRepository;
        this.subAssemblyRepository = subAssemblyRepository;
        this.partsRepository = partsRepository;
        this.projectRepository = projectRepository;
        this.authorizationService = authorizationService;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@authorizationService.hasProjectPermission(authentication, #p0, 'DESIGN_READ', 'DESIGN')")
    public DesignWorkspaceResponse workspace(@P("projectId") Long projectId, Authentication authentication) {
        requireProject(projectId);
        boolean canReadParts = hasPermission(authentication, projectId, "PARTS_READ");
        List<DesignMainAssemblyResponse> mainAssemblies = mainAssemblyRepository
                .findByProjectIdOrderById(projectId)
                .stream().map(assembly -> mainAssemblyResponse(assembly, canReadParts)).toList();
        return new DesignWorkspaceResponse(mainAssemblies, new DesignPermissions(
                hasPermission(authentication, projectId, "ASSEMBLY_CREATE"),
                hasPermission(authentication, projectId, "PARTS_CREATE"),
                canReadParts,
                hasPermission(authentication, projectId, "PARTS_UPDATE")));
    }

    @Transactional
    @PreAuthorize("@authorizationService.hasProjectPermission(authentication, #p0, 'ASSEMBLY_CREATE', 'DESIGN')")
    public DesignMainAssemblyResponse createMainAssembly(
            @P("projectId") Long projectId,
            DesignMainAssemblyRequest request,
            Authentication authentication) {
        Projects project = requireProject(projectId);
        String number = request.assemblyNumber().trim();
        if (mainAssemblyRepository.existsByProjectIdAndAssemblyNumberIgnoreCase(projectId, number)) {
            throw new IllegalArgumentException("A main assembly with this number already exists in the project");
        }

        DesignMainAssembly assembly = new DesignMainAssembly();
        assembly.setProject(project);
        assembly.setAssemblyNumber(number);
        assembly.setName(request.name().trim());
        assembly.setDescription(blankToNull(request.description()));
        assembly.setCreatedBy(authentication.getName());
        assembly = mainAssemblyRepository.save(assembly);
        auditService.record("DESIGN_MAIN_ASSEMBLY_CREATED", "DESIGN_MAIN_ASSEMBLY",
                assembly.getId(), assembly.getAssemblyNumber());
        return mainAssemblyResponse(assembly, hasPermission(authentication, projectId, "PARTS_READ"));
    }

    @Transactional
    @PreAuthorize("@authorizationService.hasProjectPermission(authentication, #p0, 'ASSEMBLY_CREATE', 'DESIGN')")
    public DesignSubAssemblyResponse createSubAssembly(
            @P("projectId") Long projectId,
            Long mainAssemblyId,
            DesignSubAssemblyRequest request,
            Authentication authentication) {
        DesignMainAssembly mainAssembly = requireMainAssembly(projectId, mainAssemblyId);
        String number = request.assemblyNumber().trim();
        if (subAssemblyRepository.existsByMainAssemblyIdAndAssemblyNumberIgnoreCase(mainAssemblyId, number)) {
            throw new IllegalArgumentException("A subassembly with this number already exists in the main assembly");
        }

        DesignSubAssembly assembly = new DesignSubAssembly();
        assembly.setMainAssembly(mainAssembly);
        assembly.setAssemblyNumber(number);
        assembly.setName(request.name().trim());
        assembly.setDescription(blankToNull(request.description()));
        assembly.setCreatedBy(authentication.getName());
        assembly = subAssemblyRepository.save(assembly);
        auditService.record("DESIGN_SUB_ASSEMBLY_CREATED", "DESIGN_SUB_ASSEMBLY",
                assembly.getId(), assembly.getAssemblyNumber());
        return subAssemblyResponse(
                assembly, hasPermission(authentication, projectId, "PARTS_READ"));
    }

    @Transactional
    @PreAuthorize("@authorizationService.hasProjectPermission(authentication, #p0, 'PARTS_CREATE', 'DESIGN')")
    public DesignPartsResponse createPart(
            @P("projectId") Long projectId,
            DesignPartsRequest request,
            Authentication authentication) {
        if (request.mainAssemblyId() == null) {
            throw new IllegalArgumentException("A main assembly is required for every part");
        }
        DesignMainAssembly mainAssembly = requireMainAssembly(projectId, request.mainAssemblyId());
        DesignSubAssembly subAssembly = request.subAssemblyId() == null
                ? null
                : requireSubAssembly(projectId, request.subAssemblyId());
        if (subAssembly != null && !subAssembly.getMainAssembly().getId().equals(mainAssembly.getId())) {
            throw new IllegalArgumentException(
                    "The selected subassembly does not belong to the selected main assembly");
        }
        if (request.startDate() != null && request.completedDate() != null
                && request.completedDate().isBefore(request.startDate())) {
            throw new IllegalArgumentException("Completed date cannot be before start date");
        }

        String number = request.partNumber().trim();
        boolean duplicate = subAssembly == null
                ? partsRepository.existsByMainAssemblyIdAndSubAssemblyIsNullAndPartNumberIgnoreCase(
                        mainAssembly.getId(), number)
                : partsRepository.existsBySubAssemblyIdAndPartNumberIgnoreCase(subAssembly.getId(), number);
        if (duplicate) {
            throw new IllegalArgumentException("A part with this number already exists in the selected assembly");
        }

        DesignParts part = new DesignParts();
        part.setMainAssembly(mainAssembly);
        part.setSubAssembly(subAssembly);
        part.setPartNumber(number);
        part.setPartName(request.partName().trim());
        part.setQuantity(request.quantity());
        part.setDescription(blankToNull(request.description()));
        part.setMake(blankToNull(request.make()));
        part.setModeOfPurchase(request.modeOfPurchase() == null
                ? ModeOfPurchase.PURCHASE
                : request.modeOfPurchase());
        part.setPartStatus(request.partStatus() == null
                ? PartStatus.NOT_STARTED
                : request.partStatus());
        part.setFilePath(blankToNull(request.filePath()));
        part.setStartDate(request.startDate());
        part.setCompletedDate(request.completedDate());
        part.setCreatedBy(authentication.getName());
        part = partsRepository.save(part);
        auditService.record("DESIGN_PART_CREATED", "DESIGN_PART", part.getId(), part.getPartNumber());
        return partResponse(part);
    }

    @Transactional
    @PreAuthorize("@authorizationService.hasProjectPermission(authentication, #p0, 'PARTS_UPDATE', 'DESIGN')")
    public DesignPartsResponse updatePartStatus(
            @P("projectId") Long projectId,
            Long partId,
            DesignPartStatusRequest request,
            Authentication authentication) {
        DesignParts part = partsRepository.findByIdAndMainAssemblyProjectId(partId, projectId)
                .orElseThrow(() -> new DesignResourceNotFoundException("Part", partId));
        PartStatus previousStatus = part.getPartStatus();
        part.setPartStatus(request.partStatus());
        part = partsRepository.save(part);
        auditService.record("DESIGN_PART_STATUS_UPDATED", "DESIGN_PART", part.getId(),
                previousStatus + " -> " + part.getPartStatus());
        return partResponse(part);
    }

    private DesignMainAssemblyResponse mainAssemblyResponse(
            DesignMainAssembly assembly, boolean includeParts) {
        List<DesignPartsResponse> parts = includeParts
                ? partsRepository.findByMainAssemblyIdOrderByPartNumber(assembly.getId())
                        .stream().filter(part -> part.getSubAssembly() == null)
                        .map(this::partResponse).toList()
                : List.of();
        List<DesignSubAssemblyResponse> subAssemblies = subAssemblyRepository
                .findByMainAssemblyIdOrderById(assembly.getId()).stream()
                .map(subAssembly -> subAssemblyResponse(subAssembly, includeParts)).toList();
        return new DesignMainAssemblyResponse(
                assembly.getId(),
                assembly.getProject().getId(),
                assembly.getAssemblyNumber(),
                assembly.getName(),
                assembly.getDescription(),
                assembly.getCreatedBy(),
                assembly.getCreatedAt(),
                parts,
                subAssemblies);
    }

    private DesignSubAssemblyResponse subAssemblyResponse(DesignSubAssembly assembly, boolean includeParts) {
        List<DesignPartsResponse> parts = includeParts
                ? partsRepository.findBySubAssemblyIdOrderByPartNumber(assembly.getId())
                        .stream().map(this::partResponse).toList()
                : List.of();
        return new DesignSubAssemblyResponse(
                assembly.getId(),
                assembly.getMainAssembly().getId(),
                assembly.getAssemblyNumber(),
                assembly.getName(),
                assembly.getDescription(),
                assembly.getCreatedBy(),
                assembly.getCreatedAt(),
                parts);
    }

    private DesignPartsResponse partResponse(DesignParts part) {
        return new DesignPartsResponse(
                part.getId(),
                part.getMainAssembly() == null
                        ? part.getSubAssembly().getMainAssembly().getId()
                        : part.getMainAssembly().getId(),
                part.getSubAssembly() == null ? null : part.getSubAssembly().getId(),
                part.getPartNumber(),
                part.getPartName(),
                part.getQuantity(),
                part.getDescription(),
                part.getMake(),
                part.getModeOfPurchase() == null ? ModeOfPurchase.PURCHASE : part.getModeOfPurchase(),
                part.getPartStatus() == null ? PartStatus.NOT_STARTED : part.getPartStatus(),
                part.getCreatedBy(),
                part.getFilePath(),
                part.getStartDate(),
                part.getCompletedDate(),
                part.getCreatedAt());
    }

    private DesignMainAssembly requireMainAssembly(Long projectId, Long assemblyId) {
        requireProject(projectId);
        return mainAssemblyRepository.findByIdAndProjectId(assemblyId, projectId)
                .orElseThrow(() -> new DesignResourceNotFoundException("Main assembly", assemblyId));
    }

    private DesignSubAssembly requireSubAssembly(Long projectId, Long assemblyId) {
        requireProject(projectId);
        return subAssemblyRepository.findByIdAndMainAssemblyProjectId(assemblyId, projectId)
                .orElseThrow(() -> new DesignResourceNotFoundException("Subassembly", assemblyId));
    }

    private Projects requireProject(Long projectId) {
        return projectRepository.findById(projectId).orElseThrow(() -> new ProjectNotFoundException(projectId));
    }

    private boolean hasPermission(Authentication authentication, Long projectId, String permission) {
        if ("PARTS_READ".equals(permission)) {
            return authorizationService.hasProjectPermission(
                    authentication, projectId, "PARTS_READ", DESIGN_AREA)
                    || authorizationService.hasProjectPermission(
                            authentication, projectId, "PARTS_UPDATE", DESIGN_AREA);
        }
        return authorizationService.hasProjectPermission(authentication, projectId, permission, DESIGN_AREA);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
