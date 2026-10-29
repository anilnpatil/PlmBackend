package com.nextfirst.plm.auth.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nextfirst.plm.auth.dto.CreateUserRequest;
import com.nextfirst.plm.auth.dto.UpdateUserRequest;
import com.nextfirst.plm.auth.dto.UpdateUserStatusRequest;
import com.nextfirst.plm.auth.dto.UserResponse;
import com.nextfirst.plm.auth.service.AdminService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {
    private final AdminService adminService;

    public AdminUserController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    @PreAuthorize("@authorizationService.hasGlobalPermission(authentication, 'USERS_MANAGE')")
    public List<UserResponse> listUsers() {
        return adminService.listUsers();
    }

    @PostMapping
    @PreAuthorize("@authorizationService.hasGlobalPermission(authentication, 'USERS_MANAGE')")
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest request) {
        return adminService.createUser(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@authorizationService.hasGlobalPermission(authentication, 'USERS_MANAGE')")
    public UserResponse updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        return adminService.updateUser(id, request);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("@authorizationService.hasGlobalPermission(authentication, 'USERS_MANAGE')")
    public UserResponse updateStatus(
            @PathVariable Long id, @Valid @RequestBody UpdateUserStatusRequest request) {
        return adminService.updateUserStatus(id, request.active());
    }
}
