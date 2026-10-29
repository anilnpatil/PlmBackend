package com.nextfirst.plm.design.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.nextfirst.plm.design.dto.DesignMainAssemblyRequest;
import com.nextfirst.plm.design.dto.DesignMainAssemblyResponse;
import com.nextfirst.plm.design.dto.DesignPartsRequest;
import com.nextfirst.plm.design.dto.DesignPartsResponse;
import com.nextfirst.plm.design.dto.DesignSubAssemblyRequest;
import com.nextfirst.plm.design.dto.DesignSubAssemblyResponse;
import com.nextfirst.plm.design.dto.DesignWorkspaceResponse;
import com.nextfirst.plm.design.service.DesignRegisterService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/projects/{projectId}/designs")
public class DesignController {
    private final DesignRegisterService registerService;

    public DesignController(DesignRegisterService registerService) {
        this.registerService = registerService;
    }

    @GetMapping({"", "/workspace"})
    public DesignWorkspaceResponse workspace(
            @P("projectId") @PathVariable Long projectId, Authentication authentication) {
        return registerService.workspace(projectId, authentication);
    }

    @PostMapping("/main-assemblies")
    @ResponseStatus(HttpStatus.CREATED)
    public DesignMainAssemblyResponse createMainAssembly(
            @P("projectId") @PathVariable Long projectId,
            @Valid @RequestBody DesignMainAssemblyRequest request,
            Authentication authentication) {
        return registerService.createMainAssembly(projectId, request, authentication);
    }

    @PostMapping("/main-assemblies/{mainAssemblyId}/sub-assemblies")
    @ResponseStatus(HttpStatus.CREATED)
    public DesignSubAssemblyResponse createSubAssembly(
            @P("projectId") @PathVariable Long projectId,
            @PathVariable Long mainAssemblyId,
            @Valid @RequestBody DesignSubAssemblyRequest request,
            Authentication authentication) {
        return registerService.createSubAssembly(projectId, mainAssemblyId, request, authentication);
    }

    @PostMapping("/parts")
    @ResponseStatus(HttpStatus.CREATED)
    public DesignPartsResponse createPart(
            @P("projectId") @PathVariable Long projectId,
            @Valid @RequestBody DesignPartsRequest request,
            Authentication authentication) {
        return registerService.createPart(projectId, request, authentication);
    }
}
