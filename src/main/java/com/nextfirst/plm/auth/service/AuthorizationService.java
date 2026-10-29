package com.nextfirst.plm.auth.service;

import java.util.Collection;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nextfirst.plm.auth.entity.AuthPermission;
import com.nextfirst.plm.auth.entity.AuthRole;
import com.nextfirst.plm.auth.entity.AuthUser;
import com.nextfirst.plm.auth.entity.ProjectAssignment;
import com.nextfirst.plm.auth.repository.AuthUserRepository;
import com.nextfirst.plm.auth.repository.ProjectAssignmentRepository;

@Service("authorizationService")
public class AuthorizationService {
    private static final String SYSTEM_ADMIN = "SYSTEM_ADMIN";

    private final AuthUserRepository userRepository;
    private final ProjectAssignmentRepository assignmentRepository;

    public AuthorizationService(
            AuthUserRepository userRepository,
            ProjectAssignmentRepository assignmentRepository) {
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @Transactional(readOnly = true)
    public boolean hasGlobalPermission(Authentication authentication, String permissionCode) {
        AuthUser user = findActiveUser(authentication);
        return user != null && (hasPermission(user.getRoles(), SYSTEM_ADMIN)
                || hasPermission(user.getRoles(), permissionCode));
    }

    @Transactional(readOnly = true)
    public boolean hasProjectPermission(
            Authentication authentication, Long projectId, String permissionCode, String functionalAreaCode) {
        AuthUser user = findActiveUser(authentication);
        if (user == null) {
            return false;
        }
        if (hasPermission(user.getRoles(), SYSTEM_ADMIN)) {
            return true;
        }
        ProjectAssignment assignment = assignmentRepository.findByProjectIdAndUserId(projectId, user.getId())
                .orElse(null);
        if (assignment == null || !hasPermission(assignment.getRoles(), permissionCode)) {
            return false;
        }
        return functionalAreaCode == null || assignment.getFunctionalAreas().stream()
                .anyMatch(area -> area.getCode().equalsIgnoreCase(functionalAreaCode));
    }

    @Transactional(readOnly = true)
    public boolean hasAnyProjectPermission(Authentication authentication, String permissionCode) {
        AuthUser user = findActiveUser(authentication);
        if (user == null) {
            return false;
        }
        return hasPermission(user.getRoles(), SYSTEM_ADMIN)
                || assignmentRepository.countByUserIdAndPermissionCode(user.getId(), permissionCode) > 0;
    }

    @Transactional(readOnly = true)
    public Long currentUserId(Authentication authentication) {
        AuthUser user = findActiveUser(authentication);
        return user == null ? null : user.getId();
    }

    @Transactional(readOnly = true)
    public boolean isSystemAdmin(Authentication authentication) {
        AuthUser user = findActiveUser(authentication);
        return user != null && hasPermission(user.getRoles(), SYSTEM_ADMIN);
    }

    private AuthUser findActiveUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return null;
        }
        return userRepository.findByEmailIgnoreCase(authentication.getName())
                .filter(AuthUser::isActive)
                .orElse(null);
    }

    private boolean hasPermission(Collection<AuthRole> roles, String permissionCode) {
        return roles.stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(AuthPermission::getCode)
                .anyMatch(code -> code.equalsIgnoreCase(permissionCode));
    }
}
