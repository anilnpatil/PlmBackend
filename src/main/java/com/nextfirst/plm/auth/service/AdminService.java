package com.nextfirst.plm.auth.service;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nextfirst.plm.auth.dto.CreateUserRequest;
import com.nextfirst.plm.auth.dto.ProjectAssignmentResponse;
import com.nextfirst.plm.auth.dto.RoleRequest;
import com.nextfirst.plm.auth.dto.RoleResponse;
import com.nextfirst.plm.auth.dto.UpdateUserRequest;
import com.nextfirst.plm.auth.dto.UserResponse;
import com.nextfirst.plm.auth.entity.AuthPermission;
import com.nextfirst.plm.auth.entity.AuthRole;
import com.nextfirst.plm.auth.entity.AuthUser;
import com.nextfirst.plm.auth.entity.FunctionalArea;
import com.nextfirst.plm.auth.entity.ProjectAssignment;
import com.nextfirst.plm.auth.exception.AuthResourceNotFoundException;
import com.nextfirst.plm.auth.repository.AuthPermissionRepository;
import com.nextfirst.plm.auth.repository.AuthRoleRepository;
import com.nextfirst.plm.auth.repository.AuthUserRepository;
import com.nextfirst.plm.auth.repository.FunctionalAreaRepository;
import com.nextfirst.plm.auth.repository.ProjectAssignmentRepository;
import com.nextfirst.plm.project.entity.Projects;
import com.nextfirst.plm.project.exception.ProjectNotFoundException;
import com.nextfirst.plm.project.repository.ProjectRepository;

@Service
public class AdminService {
    private final AuthUserRepository userRepository;
    private final AuthRoleRepository roleRepository;
    private final AuthPermissionRepository permissionRepository;
    private final FunctionalAreaRepository areaRepository;
    private final ProjectAssignmentRepository assignmentRepository;
    private final ProjectRepository projectRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthAuditService auditService;

    public AdminService(
            AuthUserRepository userRepository,
            AuthRoleRepository roleRepository,
            AuthPermissionRepository permissionRepository,
            FunctionalAreaRepository areaRepository,
            ProjectAssignmentRepository assignmentRepository,
            ProjectRepository projectRepository,
            PasswordEncoder passwordEncoder,
            AuthAuditService auditService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.areaRepository = areaRepository;
        this.assignmentRepository = assignmentRepository;
        this.projectRepository = projectRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@authorizationService.hasGlobalPermission(authentication, 'USERS_MANAGE')")
    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream().map(this::toUserResponse).toList();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@authorizationService.hasProjectPermission(authentication, #p0, 'PROJECT_ASSIGN', null)")
    public List<UserResponse> listAssignableUsers(Long projectId) {
        return userRepository.findAll().stream()
                .filter(AuthUser::isActive)
                .map(this::toUserResponse)
                .toList();
    }

    @Transactional
    @PreAuthorize("@authorizationService.hasGlobalPermission(authentication, 'USERS_MANAGE')")
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email().trim())) {
            throw new IllegalArgumentException("A user with that email already exists");
        }
        AuthUser user = new AuthUser();
        user.setEmail(request.email().trim().toLowerCase());
        user.setFullName(request.fullName().trim());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.getRoles().addAll(resolveRoles(request.roleCodes()));
        user = userRepository.save(user);
        auditService.record("USER_CREATED", "USER", user.getId(),
                user.getEmail() + ", roles=" + user.getRoles().stream().map(AuthRole::getCode).toList());
        return toUserResponse(user);
    }

    @Transactional
    @PreAuthorize("@authorizationService.hasGlobalPermission(authentication, 'USERS_MANAGE')")
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        AuthUser user = findUser(id);
        user.setFullName(request.fullName().trim());
        user.getRoles().clear();
        user.getRoles().addAll(resolveRoles(request.roleCodes()));
        if (request.password() != null && !request.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        auditService.record("USER_UPDATED", "USER", user.getId(),
                "Profile or password updated, roles="
                        + user.getRoles().stream().map(AuthRole::getCode).toList());
        return toUserResponse(user);
    }

    @Transactional
    @PreAuthorize("@authorizationService.hasGlobalPermission(authentication, 'USERS_MANAGE')")
    public UserResponse updateUserStatus(Long id, boolean active) {
        AuthUser user = findUser(id);
        user.setActive(active);
        auditService.record(active ? "USER_ACTIVATED" : "USER_DEACTIVATED", "USER", id, user.getEmail());
        return toUserResponse(user);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@authorizationService.hasGlobalPermission(authentication, 'ROLES_MANAGE') or @authorizationService.hasGlobalPermission(authentication, 'USERS_MANAGE') or @authorizationService.hasAnyProjectPermission(authentication, 'PROJECT_ASSIGN')")
    public List<RoleResponse> listRoles() {
        return roleRepository.findAll().stream().map(this::toRoleResponse).toList();
    }

    @Transactional
    @PreAuthorize("@authorizationService.hasGlobalPermission(authentication, 'ROLES_MANAGE')")
    public RoleResponse createRole(RoleRequest request) {
        if (roleRepository.existsByCodeIgnoreCase(request.code())) {
            throw new IllegalArgumentException("A role with that code already exists");
        }
        AuthRole role = new AuthRole();
        role.setCode(request.code());
        updateRole(role, request);
        role = roleRepository.save(role);
        auditService.record("ROLE_CREATED", "ROLE", role.getId(),
                role.getCode() + ", permissions=" + request.permissionCodes());
        return toRoleResponse(role);
    }

    @Transactional
    @PreAuthorize("@authorizationService.hasGlobalPermission(authentication, 'ROLES_MANAGE')")
    public RoleResponse updateRole(Long id, RoleRequest request) {
        AuthRole role = roleRepository.findById(id)
                .orElseThrow(() -> new AuthResourceNotFoundException("Role", id));
        if (!role.getCode().equalsIgnoreCase(request.code())
                && roleRepository.existsByCodeIgnoreCase(request.code())) {
            throw new IllegalArgumentException("A role with that code already exists");
        }
        role.setCode(request.code());
        updateRole(role, request);
        auditService.record("ROLE_UPDATED", "ROLE", id,
                role.getCode() + ", permissions=" + request.permissionCodes());
        return toRoleResponse(role);
    }

    @Transactional
    @PreAuthorize("@authorizationService.hasProjectPermission(authentication, #p0, 'PROJECT_ASSIGN', null)")
    public List<ProjectAssignmentResponse> listAssignments(Long projectId) {
        requireProject(projectId);
        return assignmentRepository.findByProjectIdOrderByUserFullName(projectId).stream()
                .map(this::toAssignmentResponse)
                .toList();
    }

    @Transactional
    @PreAuthorize("@authorizationService.hasProjectPermission(authentication, #p0, 'PROJECT_ASSIGN', null)")
    public ProjectAssignmentResponse assignUser(
            Long projectId, Long userId, Set<String> roleCodes, Set<String> areaCodes) {
        Projects project = requireProject(projectId);
        AuthUser user = findUser(userId);
        Set<AuthRole> requestedRoles = resolveRoles(roleCodes);
        if (requestedRoles.stream().flatMap(role -> role.getPermissions().stream())
                .anyMatch(permission -> permission.getCode().equalsIgnoreCase("SYSTEM_ADMIN"))) {
            throw new IllegalArgumentException("The SYSTEM_ADMIN permission cannot be assigned at project level");
        }
        ProjectAssignment assignment = assignmentRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseGet(ProjectAssignment::new);
        boolean created = assignment.getId() == null;
        assignment.setProject(project);
        assignment.setUser(user);
        assignment.getRoles().clear();
        assignment.getRoles().addAll(requestedRoles);
        assignment.getFunctionalAreas().clear();
        assignment.getFunctionalAreas().addAll(resolveAreas(areaCodes));
        assignment = assignmentRepository.save(assignment);
        auditService.record(created ? "PROJECT_USER_ASSIGNED" : "PROJECT_ASSIGNMENT_UPDATED",
                "PROJECT_ASSIGNMENT", assignment.getId(),
                "projectId=" + projectId + ", userId=" + userId
                        + ", roles=" + sorted(roleCodes) + ", areas=" + sorted(areaCodes));
        return toAssignmentResponse(assignment);
    }

    @Transactional
    @PreAuthorize("@authorizationService.hasProjectPermission(authentication, #p0, 'PROJECT_ASSIGN', null)")
    public void removeAssignment(Long projectId, Long userId) {
        ProjectAssignment assignment = assignmentRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new AuthResourceNotFoundException(
                        "Project assignment", projectId + "/" + userId));
        assignmentRepository.delete(assignment);
        auditService.record("PROJECT_USER_UNASSIGNED", "PROJECT_ASSIGNMENT", assignment.getId(),
                "projectId=" + projectId + ", userId=" + userId);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@authorizationService.hasGlobalPermission(authentication, 'ROLES_MANAGE') or @authorizationService.hasAnyProjectPermission(authentication, 'PROJECT_ASSIGN')")
    public List<FunctionalAreaResponse> listFunctionalAreas() {
        return areaRepository.findAll().stream()
                .map(area -> new FunctionalAreaResponse(area.getCode(), area.getName()))
                .toList();
    }

    @Transactional
    @PreAuthorize("@authorizationService.hasGlobalPermission(authentication, 'ROLES_MANAGE')")
    public FunctionalAreaResponse createFunctionalArea(String code, String name) {
        String normalizedCode = code.trim().toUpperCase();
        if (areaRepository.findByCodeIgnoreCase(normalizedCode).isPresent()) {
            throw new IllegalArgumentException("A functional area with that code already exists");
        }
        FunctionalArea area = areaRepository.save(new FunctionalArea(normalizedCode, name.trim()));
        auditService.record("FUNCTIONAL_AREA_CREATED", "FUNCTIONAL_AREA", area.getId(), area.getCode());
        return new FunctionalAreaResponse(area.getCode(), area.getName());
    }

    private void updateRole(AuthRole role, RoleRequest request) {
        role.setName(request.name().trim());
        role.setDescription(request.description());
        role.getPermissions().clear();
        role.getPermissions().addAll(resolvePermissions(request.permissionCodes()));
    }

    private Set<AuthPermission> resolvePermissions(Set<String> codes) {
        return codes.stream().map(code -> {
            String normalized = code.trim().toUpperCase();
            return permissionRepository.findByCodeIgnoreCase(normalized)
                    .orElseGet(() -> permissionRepository.save(new AuthPermission(
                            normalized, normalized.replace('_', ' '), "Dynamically created permission")));
        }).collect(Collectors.toSet());
    }

    private Set<AuthRole> resolveRoles(Set<String> codes) {
        return codes.stream()
                .map(code -> roleRepository.findByCodeIgnoreCase(code.trim())
                        .orElseThrow(() -> new IllegalArgumentException("Unknown role code: " + code)))
                .collect(Collectors.toSet());
    }

    private Set<FunctionalArea> resolveAreas(Set<String> codes) {
        return codes.stream()
                .map(code -> areaRepository.findByCodeIgnoreCase(code.trim())
                        .orElseThrow(() -> new IllegalArgumentException("Unknown functional area code: " + code)))
                .collect(Collectors.toSet());
    }

    private AuthUser findUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new AuthResourceNotFoundException("User", id));
    }

    private Projects requireProject(Long id) {
        return projectRepository.findById(id).orElseThrow(() -> new ProjectNotFoundException(id));
    }

    private UserResponse toUserResponse(AuthUser user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getFullName(), user.isActive(),
                user.getRoles().stream().map(AuthRole::getCode).collect(Collectors.toCollection(TreeSet::new)));
    }

    private RoleResponse toRoleResponse(AuthRole role) {
        return new RoleResponse(role.getId(), role.getCode(), role.getName(), role.getDescription(),
                role.getPermissions().stream().map(AuthPermission::getCode)
                        .collect(Collectors.toCollection(TreeSet::new)));
    }

    private ProjectAssignmentResponse toAssignmentResponse(ProjectAssignment assignment) {
        return new ProjectAssignmentResponse(
                assignment.getUser().getId(),
                assignment.getUser().getEmail(),
                assignment.getUser().getFullName(),
                assignment.getRoles().stream().map(AuthRole::getCode)
                        .collect(Collectors.toCollection(TreeSet::new)),
                assignment.getFunctionalAreas().stream().map(FunctionalArea::getCode)
                        .collect(Collectors.toCollection(TreeSet::new)),
                assignment.getAssignedAt());
    }

    private Set<String> sorted(Set<String> values) {
        return new TreeSet<>(values);
    }

    public record FunctionalAreaResponse(String code, String name) {}
}
