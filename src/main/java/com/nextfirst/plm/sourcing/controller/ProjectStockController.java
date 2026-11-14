package com.nextfirst.plm.sourcing.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.nextfirst.plm.sourcing.dto.ProjectStockComponentResponse;
import com.nextfirst.plm.sourcing.dto.ProjectStockUpdateRequest;
import com.nextfirst.plm.sourcing.service.ProjectStockService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/projects/{projectId}/stock/components")
public class ProjectStockController {
    private final ProjectStockService projectStockService;

    public ProjectStockController(ProjectStockService projectStockService) {
        this.projectStockService = projectStockService;
    }

    @GetMapping
    public List<ProjectStockComponentResponse> components(
            @P("projectId") @PathVariable Long projectId,
            Authentication authentication) {
        return projectStockService.components(projectId, authentication);
    }

    @PutMapping("/{designPartId}")
    @ResponseStatus(HttpStatus.OK)
    public ProjectStockComponentResponse update(
            @P("projectId") @PathVariable Long projectId,
            @PathVariable Long designPartId,
            @Valid @RequestBody ProjectStockUpdateRequest request,
            Authentication authentication) {
        return projectStockService.update(projectId, designPartId, request, authentication);
    }
}
