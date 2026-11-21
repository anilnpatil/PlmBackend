package com.nextfirst.plm.assembly.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.nextfirst.plm.assembly.dto.AssemblyComponentResponse;
import com.nextfirst.plm.assembly.service.AssemblyComponentsService;

@RestController
@RequestMapping("/api/projects/{projectId}/assembly/components")
public class AssemblyComponentsController {
    private final AssemblyComponentsService assemblyComponentsService;

    public AssemblyComponentsController(AssemblyComponentsService assemblyComponentsService) {
        this.assemblyComponentsService = assemblyComponentsService;
    }

    @GetMapping
    public List<AssemblyComponentResponse> components(
            @P("projectId") @PathVariable Long projectId,
            Authentication authentication) {
        return assemblyComponentsService.components(projectId, authentication);
    }

    @PostMapping("/{designPartId}/start")
    @ResponseStatus(HttpStatus.OK)
    public AssemblyComponentResponse start(
            @P("projectId") @PathVariable Long projectId,
            @PathVariable Long designPartId,
            Authentication authentication) {
        return assemblyComponentsService.start(projectId, designPartId, authentication);
    }

    @PostMapping("/{designPartId}/complete")
    @ResponseStatus(HttpStatus.OK)
    public AssemblyComponentResponse complete(
            @P("projectId") @PathVariable Long projectId,
            @PathVariable Long designPartId,
            Authentication authentication) {
        return assemblyComponentsService.complete(projectId, designPartId, authentication);
    }
}
