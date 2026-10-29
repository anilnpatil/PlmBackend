package com.nextfirst.plm.auth.service;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nextfirst.plm.auth.entity.AuthPermission;
import com.nextfirst.plm.auth.entity.AuthRole;
import com.nextfirst.plm.auth.entity.AuthUser;
import com.nextfirst.plm.auth.entity.FunctionalArea;
import com.nextfirst.plm.auth.entity.AuthAuditEvent;
import com.nextfirst.plm.auth.repository.AuthAuditEventRepository;
import com.nextfirst.plm.auth.repository.AuthPermissionRepository;
import com.nextfirst.plm.auth.repository.AuthRoleRepository;
import com.nextfirst.plm.auth.repository.AuthUserRepository;
import com.nextfirst.plm.auth.repository.FunctionalAreaRepository;

@Component
public class AuthBootstrapRunner implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(AuthBootstrapRunner.class);

    private static final Set<String> PERMISSIONS = Set.of(
            "SYSTEM_ADMIN", "USERS_MANAGE", "ROLES_MANAGE", "AUDIT_READ",
            "PROJECT_CREATE", "PROJECT_READ", "PROJECT_UPDATE", "PROJECT_DELETE", "PROJECT_ASSIGN",
            "DESIGN_CREATE", "DESIGN_READ", "DESIGN_UPDATE", "DESIGN_SUBMIT", "DESIGN_REVIEW", "DESIGN_APPROVE",
            "DESIGN_RETURN", "PARTS_CREATE", "PARTS_READ", "PARTS_UPDATE",
            "ASSEMBLY_CREATE", "ASSEMBLY_READ", "ASSEMBLY_UPDATE",
            "STORE_RECEIPT_MANAGE", "STORE_UPDATE",
            "ASSEMBLY_PROGRESS_UPDATE", "CONTROLS_TASK_MANAGE", "CONTROLS_TASK_UPDATE",
            "SOURCING_SUPPLIER_MANAGE", "SOURCING_QUOTE_MANAGE", "SOURCING_COST_MANAGE",
            "SOURCING_PURCHASE_MANAGE");

    private static final Map<String, Set<String>> STARTER_ROLES = starterRoles();

    private final AuthUserRepository userRepository;
    private final AuthRoleRepository roleRepository;
    private final AuthPermissionRepository permissionRepository;
    private final FunctionalAreaRepository areaRepository;
    private final AuthAuditEventRepository auditRepository;
    private final PasswordEncoder passwordEncoder;
    private final String bootstrapEmail;
    private final String bootstrapPassword;
    private final String bootstrapFullName;

    public AuthBootstrapRunner(
            AuthUserRepository userRepository,
            AuthRoleRepository roleRepository,
            AuthPermissionRepository permissionRepository,
            FunctionalAreaRepository areaRepository,
            AuthAuditEventRepository auditRepository,
            PasswordEncoder passwordEncoder,
            @Value("${plm.auth.bootstrap.email:}") String bootstrapEmail,
            @Value("${plm.auth.bootstrap.password:}") String bootstrapPassword,
            @Value("${plm.auth.bootstrap.full-name:PLM Administrator}") String bootstrapFullName) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.areaRepository = areaRepository;
        this.auditRepository = auditRepository;
        this.passwordEncoder = passwordEncoder;
        this.bootstrapEmail = bootstrapEmail;
        this.bootstrapPassword = bootstrapPassword;
        this.bootstrapFullName = bootstrapFullName;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (String code : PERMISSIONS) {
            permissionRepository.findByCodeIgnoreCase(code)
                    .orElseGet(() -> permissionRepository.save(
                            new AuthPermission(code, code.replace('_', ' '), "PLM authorization permission")));
        }
        for (Map.Entry<String, Set<String>> roleDefinition : STARTER_ROLES.entrySet()) {
            if (roleRepository.findByCodeIgnoreCase(roleDefinition.getKey()).isEmpty()) {
                AuthRole role = new AuthRole();
                role.setCode(roleDefinition.getKey());
                role.setName(roleDefinition.getKey().replace('_', ' '));
                role.setDescription("Initial configurable PLM role");
                role.getPermissions().addAll(roleDefinition.getValue().stream()
                        .map(code -> permissionRepository.findByCodeIgnoreCase(code).orElseThrow())
                        .toList());
                roleRepository.save(role);
            }
        }
        Map<String, String> areas = Map.of(
                "PROJECT", "Project",
                "DESIGN", "Design",
                "REVIEW", "Review",
                "STORE", "Store",
                "ASSEMBLY", "Assembly",
                "CONTROLS", "Controls",
                "SOURCING", "Sourcing");
        areas.forEach((code, name) -> areaRepository.findByCodeIgnoreCase(code)
                .orElseGet(() -> areaRepository.save(new FunctionalArea(code, name))));

        if (bootstrapEmail.isBlank() && bootstrapPassword.isBlank()) {
            logger.warn("No bootstrap admin configured; set PLM_BOOTSTRAP_ADMIN_EMAIL and PLM_BOOTSTRAP_ADMIN_PASSWORD to provision the first administrator");
            return;
        }
        if (bootstrapEmail.isBlank() || bootstrapPassword.isBlank()) {
            throw new IllegalStateException("Both PLM_BOOTSTRAP_ADMIN_EMAIL and PLM_BOOTSTRAP_ADMIN_PASSWORD must be set");
        }
        int passwordBytes = bootstrapPassword.getBytes(StandardCharsets.UTF_8).length;
        if (bootstrapPassword.length() < 12 || passwordBytes > 72) {
            throw new IllegalStateException("Bootstrap admin password must be at least 12 characters and no more than 72 UTF-8 bytes");
        }
        if (userRepository.existsByEmailIgnoreCase(bootstrapEmail.trim())) {
            logger.info("Bootstrap admin already exists; no user or role changes were made");
            return;
        }
        AuthUser admin = new AuthUser();
        admin.setEmail(bootstrapEmail.trim().toLowerCase());
        admin.setFullName(bootstrapFullName.trim());
        admin.setPasswordHash(passwordEncoder.encode(bootstrapPassword));
        admin.getRoles().add(roleRepository.findByCodeIgnoreCase("SYSTEM_ADMIN").orElseThrow());
        admin = userRepository.save(admin);
        auditRepository.save(new AuthAuditEvent(
                "system", "BOOTSTRAP_ADMIN_CREATED", "USER", admin.getId().toString(), admin.getEmail()));
        logger.info("Provisioned the initial administrator account from environment configuration");
    }

    private static Map<String, Set<String>> starterRoles() {
        Map<String, Set<String>> roles = new LinkedHashMap<>();
        roles.put("SYSTEM_ADMIN", Set.of(
                "SYSTEM_ADMIN", "USERS_MANAGE", "ROLES_MANAGE", "AUDIT_READ",
                "PROJECT_CREATE", "PROJECT_READ", "PROJECT_UPDATE", "PROJECT_DELETE", "PROJECT_ASSIGN"));
        roles.put("PROJECT_MANAGER", Set.of("PROJECT_READ", "PROJECT_UPDATE", "PROJECT_ASSIGN"));
        roles.put("DESIGNER", Set.of(
                "PROJECT_READ", "DESIGN_CREATE", "DESIGN_READ", "DESIGN_UPDATE", "DESIGN_SUBMIT",
                "ASSEMBLY_CREATE", "ASSEMBLY_READ", "ASSEMBLY_UPDATE",
                "PARTS_CREATE", "PARTS_READ", "PARTS_UPDATE"));
        roles.put("REVIEWER", Set.of(
                "PROJECT_READ", "DESIGN_REVIEW", "DESIGN_APPROVE", "DESIGN_RETURN", "DESIGN_READ"));
        roles.put("STORE", Set.of("PROJECT_READ", "STORE_RECEIPT_MANAGE", "STORE_UPDATE", "PARTS_READ"));
        roles.put("ASSEMBLY", Set.of(
                "PROJECT_READ", "ASSEMBLY_READ", "ASSEMBLY_PROGRESS_UPDATE", "PARTS_READ"));
        roles.put("CONTROLS", Set.of("PROJECT_READ", "CONTROLS_TASK_MANAGE", "CONTROLS_TASK_UPDATE"));
        roles.put("SOURCING", Set.of(
                "PROJECT_READ", "SOURCING_SUPPLIER_MANAGE", "SOURCING_QUOTE_MANAGE",
                "SOURCING_COST_MANAGE", "SOURCING_PURCHASE_MANAGE"));
        return roles;
    }
}
