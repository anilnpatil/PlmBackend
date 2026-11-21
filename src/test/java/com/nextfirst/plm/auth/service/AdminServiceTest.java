package com.nextfirst.plm.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.nextfirst.plm.auth.entity.AreaAccessLevel;
import com.nextfirst.plm.auth.entity.AuthUser;
import com.nextfirst.plm.auth.entity.FunctionalArea;
import com.nextfirst.plm.auth.entity.ProjectAreaAccess;
import com.nextfirst.plm.auth.entity.ProjectAssignment;
import com.nextfirst.plm.auth.repository.AuthPermissionRepository;
import com.nextfirst.plm.auth.repository.AuthRoleRepository;
import com.nextfirst.plm.auth.repository.AuthUserRepository;
import com.nextfirst.plm.auth.repository.FunctionalAreaRepository;
import com.nextfirst.plm.auth.repository.ProjectAssignmentRepository;
import com.nextfirst.plm.project.entity.Projects;
import com.nextfirst.plm.project.repository.ProjectRepository;

class AdminServiceTest {
    @Mock
    private AuthUserRepository userRepository;

    @Mock
    private AuthRoleRepository roleRepository;

    @Mock
    private AuthPermissionRepository permissionRepository;

    @Mock
    private FunctionalAreaRepository areaRepository;

    @Mock
    private ProjectAssignmentRepository assignmentRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthAuditService auditService;

    private AdminService adminService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        adminService = new AdminService(
                userRepository, roleRepository, permissionRepository, areaRepository,
                assignmentRepository, projectRepository, passwordEncoder, auditService);
    }

    @Test
    void updatesExistingAreaGrantInsteadOfReplacingIt() {
        Long projectId = 51L;
        Long userId = 14L;
        Projects project = new Projects();
        AuthUser user = new AuthUser();
        user.setEmail("designer@example.com");
        user.setFullName("Project Designer");
        ReflectionTestUtils.setField(user, "id", userId);

        FunctionalArea design = new FunctionalArea("DESIGN", "Design");
        ProjectAssignment assignment = new ProjectAssignment();
        ReflectionTestUtils.setField(assignment, "id", 100L);
        assignment.setProject(project);
        assignment.setUser(user);
        ProjectAreaAccess existingGrant = new ProjectAreaAccess();
        existingGrant.setAssignment(assignment);
        existingGrant.setArea(design);
        existingGrant.setAccessLevel(AreaAccessLevel.READ_ONLY);
        assignment.getAreaAccessGrants().add(existingGrant);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(assignmentRepository.findByProjectIdAndUserId(projectId, userId))
                .thenReturn(Optional.of(assignment));
        when(areaRepository.findByCodeIgnoreCase("DESIGN")).thenReturn(Optional.of(design));
        when(assignmentRepository.save(assignment)).thenReturn(assignment);

        adminService.assignUser(projectId, userId, Set.of(), Set.of(), Map.of("DESIGN", "READ_WRITE"));

        assertEquals(1, assignment.getAreaAccessGrants().size());
        assertSame(existingGrant, assignment.getAreaAccessGrants().iterator().next());
        assertEquals(AreaAccessLevel.READ_WRITE, existingGrant.getAccessLevel());
    }
}
