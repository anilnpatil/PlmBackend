package com.nextfirst.plm.supplier.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.nextfirst.plm.supplier.dto.SupplierPageResponse;
import com.nextfirst.plm.supplier.dto.SupplierResponse;
import com.nextfirst.plm.supplier.dto.SupplierStatusRequest;
import com.nextfirst.plm.supplier.dto.SupplierSuggestion;
import com.nextfirst.plm.supplier.dto.SupplierUpsertRequest;
import com.nextfirst.plm.supplier.entity.SupplierServiceType;
import com.nextfirst.plm.supplier.service.SupplierService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @GetMapping("/suggestions")
    @PreAuthorize(
            "@authorizationService.hasGlobalPermission(authentication, 'SOURCING_SUPPLIER_MANAGE')"
                    + " or @authorizationService.hasAnyProjectPermission(authentication, 'PURCHASE_COMPONENTS_READ')"
                    + " or @authorizationService.hasAnyProjectPermission(authentication, 'MANUFACTURING_COMPONENTS_READ')")
    public List<SupplierSuggestion> suggestions(
            @RequestParam(defaultValue = "") String query,
            @RequestParam SupplierServiceType serviceType) {
        return supplierService.suggestions(query, serviceType);
    }

    @GetMapping
    @PreAuthorize(
            "@authorizationService.hasGlobalPermission(authentication, 'SOURCING_SUPPLIER_MANAGE')")
    public SupplierPageResponse search(
            @RequestParam(defaultValue = "") String query,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return supplierService.search(query, active, page, size);
    }

    @GetMapping("/{id}")
    @PreAuthorize(
            "@authorizationService.hasGlobalPermission(authentication, 'SOURCING_SUPPLIER_MANAGE')")
    public SupplierResponse get(@PathVariable Long id) {
        return supplierService.get(id);
    }

    @PostMapping
    @PreAuthorize(
            "@authorizationService.hasGlobalPermission(authentication, 'SOURCING_SUPPLIER_MANAGE')")
    public ResponseEntity<SupplierResponse> create(@Valid @RequestBody SupplierUpsertRequest request) {
        SupplierResponse supplier = supplierService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(supplier.id())
                .toUri();
        return ResponseEntity.created(location).body(supplier);
    }

    @PutMapping("/{id}")
    @PreAuthorize(
            "@authorizationService.hasGlobalPermission(authentication, 'SOURCING_SUPPLIER_MANAGE')")
    public SupplierResponse update(
            @PathVariable Long id,
            @Valid @RequestBody SupplierUpsertRequest request) {
        return supplierService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize(
            "@authorizationService.hasGlobalPermission(authentication, 'SOURCING_SUPPLIER_MANAGE')")
    public SupplierResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody SupplierStatusRequest request) {
        return supplierService.updateStatus(id, request.active());
    }
}
