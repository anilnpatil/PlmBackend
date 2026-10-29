package com.nextfirst.plm.auth.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nextfirst.plm.auth.dto.FunctionalAreaRequest;
import com.nextfirst.plm.auth.dto.RoleRequest;
import com.nextfirst.plm.auth.dto.RoleResponse;
import com.nextfirst.plm.auth.service.AdminService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin")
public class AdminRoleController {
    private final AdminService adminService;

    public AdminRoleController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/roles")
    @PreAuthorize("@authorizationService.hasGlobalPermission(authentication, 'ROLES_MANAGE') or @authorizationService.hasGlobalPermission(authentication, 'USERS_MANAGE') or @authorizationService.hasAnyProjectPermission(authentication, 'PROJECT_ASSIGN')")
    public List<RoleResponse> listRoles() {
        return adminService.listRoles();
    }

    @PostMapping("/roles")
    @PreAuthorize("@authorizationService.hasGlobalPermission(authentication, 'ROLES_MANAGE')")
    public RoleResponse createRole(@Valid @RequestBody RoleRequest request) {
        return adminService.createRole(request);
    }

    @PutMapping("/roles/{id}")
    @PreAuthorize("@authorizationService.hasGlobalPermission(authentication, 'ROLES_MANAGE')")
    public RoleResponse updateRole(@PathVariable Long id, @Valid @RequestBody RoleRequest request) {
        return adminService.updateRole(id, request);
    }

    @GetMapping("/functional-areas")
    @PreAuthorize("@authorizationService.hasGlobalPermission(authentication, 'ROLES_MANAGE') or @authorizationService.hasAnyProjectPermission(authentication, 'PROJECT_ASSIGN')")
    public List<AdminService.FunctionalAreaResponse> listFunctionalAreas() {
        return adminService.listFunctionalAreas();
    }

    @PostMapping("/functional-areas")
    @PreAuthorize("@authorizationService.hasGlobalPermission(authentication, 'ROLES_MANAGE')")
    public AdminService.FunctionalAreaResponse createFunctionalArea(
            @Valid @RequestBody FunctionalAreaRequest request) {
        return adminService.createFunctionalArea(request.code(), request.name());
    }
}
