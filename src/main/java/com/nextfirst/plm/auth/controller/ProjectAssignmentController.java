package com.nextfirst.plm.auth.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nextfirst.plm.auth.dto.ProjectAssignmentRequest;
import com.nextfirst.plm.auth.dto.ProjectAssignmentResponse;
import com.nextfirst.plm.auth.dto.UserResponse;
import com.nextfirst.plm.auth.service.AdminService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/projects/{projectId}")
public class ProjectAssignmentController {
    private final AdminService adminService;

    public ProjectAssignmentController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/assignments")
    @PreAuthorize("@authorizationService.hasProjectPermission(authentication, #p0, 'PROJECT_ASSIGN', null)")
    public List<ProjectAssignmentResponse> listAssignments(@P("projectId") @PathVariable Long projectId) {
        return adminService.listAssignments(projectId);
    }

    @GetMapping("/assignable-users")
    @PreAuthorize("@authorizationService.hasProjectPermission(authentication, #p0, 'PROJECT_ASSIGN', null)")
    public List<UserResponse> listAssignableUsers(
            @P("projectId") @PathVariable Long projectId) {
        return adminService.listAssignableUsers(projectId);
    }

    @PutMapping("/assignments/{userId}")
    @PreAuthorize("@authorizationService.hasProjectPermission(authentication, #p0, 'PROJECT_ASSIGN', null)")
    public ProjectAssignmentResponse assignUser(
            @P("projectId") @PathVariable Long projectId,
            @PathVariable Long userId,
            @Valid @RequestBody ProjectAssignmentRequest request) {
        return adminService.assignUser(
                projectId, userId, request.roleCodes(), request.functionalAreaCodes());
    }

    @DeleteMapping("/assignments/{userId}")
    @PreAuthorize("@authorizationService.hasProjectPermission(authentication, #p0, 'PROJECT_ASSIGN', null)")
    public void removeAssignment(
            @P("projectId") @PathVariable Long projectId, @PathVariable Long userId) {
        adminService.removeAssignment(projectId, userId);
    }
}
