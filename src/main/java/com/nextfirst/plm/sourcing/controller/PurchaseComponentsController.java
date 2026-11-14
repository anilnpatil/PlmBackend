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

import com.nextfirst.plm.sourcing.dto.ProcurementComponentResponse;
import com.nextfirst.plm.sourcing.dto.ProcurementWorkflowRequest;
import com.nextfirst.plm.sourcing.service.PurchaseComponentsService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/projects/{projectId}/purchase/components")
public class PurchaseComponentsController {
    private final PurchaseComponentsService purchaseComponentsService;

    public PurchaseComponentsController(PurchaseComponentsService purchaseComponentsService) {
        this.purchaseComponentsService = purchaseComponentsService;
    }

    @GetMapping
    public List<ProcurementComponentResponse> components(
            @P("projectId") @PathVariable Long projectId,
            Authentication authentication) {
        return purchaseComponentsService.components(projectId, authentication);
    }

    @PutMapping("/{designPartId}")
    @ResponseStatus(HttpStatus.OK)
    public ProcurementComponentResponse update(
            @P("projectId") @PathVariable Long projectId,
            @PathVariable Long designPartId,
            @Valid @RequestBody ProcurementWorkflowRequest request,
            Authentication authentication) {
        return purchaseComponentsService.update(projectId, designPartId, request, authentication);
    }
}
