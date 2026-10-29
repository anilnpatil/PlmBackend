package com.nextfirst.plm.auth.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import com.nextfirst.plm.auth.entity.AuthPermission;
import com.nextfirst.plm.auth.entity.AuthRole;
import com.nextfirst.plm.auth.entity.AuthUser;
import com.nextfirst.plm.auth.entity.FunctionalArea;
import com.nextfirst.plm.auth.entity.ProjectAssignment;
import com.nextfirst.plm.auth.repository.AuthUserRepository;
import com.nextfirst.plm.auth.repository.ProjectAssignmentRepository;

class AuthorizationServiceTest {
    @Mock
    private AuthUserRepository userRepository;

    @Mock
    private ProjectAssignmentRepository assignmentRepository;

    private AuthorizationService authorizationService;
    private AuthUser user;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authorizationService = new AuthorizationService(userRepository, assignmentRepository);
        user = new AuthUser();
        user.setEmail("designer@example.com");
        ReflectionTestUtils.setField(user, "id", 14L);
        authentication = UsernamePasswordAuthenticationToken.authenticated(
                user.getEmail(), "token", List.of(new SimpleGrantedAuthority("PLM_USER")));
        when(userRepository.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
    }

    @Test
    void requiresProjectMembershipRolePermissionAndAssignedFunctionalArea() {
        AuthRole designer = roleWithPermission("DESIGNER", "DESIGN_UPDATE");
        ProjectAssignment assignment = new ProjectAssignment();
        assignment.setUser(user);
        assignment.getRoles().add(designer);
        assignment.getFunctionalAreas().add(new FunctionalArea("DESIGN", "Design"));
        when(assignmentRepository.findByProjectIdAndUserId(51L, 14L)).thenReturn(Optional.of(assignment));
        when(assignmentRepository.findByProjectIdAndUserId(52L, 14L)).thenReturn(Optional.empty());

        assertTrue(authorizationService.hasProjectPermission(authentication, 51L, "DESIGN_UPDATE", "DESIGN"));
        assertFalse(authorizationService.hasProjectPermission(authentication, 51L, "DESIGN_UPDATE", "STORE"));
        assertFalse(authorizationService.hasProjectPermission(authentication, 51L, "DESIGN_DELETE", "DESIGN"));
        assertFalse(authorizationService.hasProjectPermission(authentication, 52L, "DESIGN_UPDATE", "DESIGN"));
    }

    @Test
    void systemAdminPermissionBypassesProjectMembership() {
        user.getRoles().add(roleWithPermission("SYSTEM_ADMINISTRATOR", "SYSTEM_ADMIN"));

        assertTrue(authorizationService.hasProjectPermission(
                authentication, 987L, "PROJECT_DELETE", "SOURCING"));
    }

    @Test
    void detectsProjectPermissionsForScopedSearch() {
        when(assignmentRepository.countByUserIdAndPermissionCode(14L, "PROJECT_READ")).thenReturn(1L);

        assertTrue(authorizationService.hasAnyProjectPermission(authentication, "PROJECT_READ"));
    }

    private AuthRole roleWithPermission(String code, String permissionCode) {
        AuthRole role = new AuthRole();
        role.setCode(code);
        role.getPermissions().add(new AuthPermission(permissionCode, permissionCode, ""));
        return role;
    }
}
