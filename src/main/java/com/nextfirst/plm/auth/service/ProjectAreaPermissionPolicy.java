package com.nextfirst.plm.auth.service;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.nextfirst.plm.auth.entity.AreaAccessLevel;

final class ProjectAreaPermissionPolicy {
    private static final Map<String, AreaPolicy> POLICIES = Map.of(
            "DESIGN", new AreaPolicy(
                    Set.of("DESIGN_READ", "PARTS_READ", "ASSEMBLY_READ"),
                    Set.of("DESIGN_CREATE", "DESIGN_UPDATE", "DESIGN_SUBMIT",
                            "PARTS_CREATE", "PARTS_UPDATE", "ASSEMBLY_CREATE", "ASSEMBLY_UPDATE")),
            "PURCHASE", new AreaPolicy(
                    Set.of("PURCHASE_COMPONENTS_READ"),
                    Set.of("PURCHASE_COMPONENTS_UPDATE", "SOURCING_SUPPLIER_MANAGE",
                            "SOURCING_QUOTE_MANAGE", "SOURCING_COST_MANAGE", "SOURCING_PURCHASE_MANAGE")),
            "MANUFACTURING", new AreaPolicy(
                    Set.of("MANUFACTURING_COMPONENTS_READ"),
                    Set.of("MANUFACTURING_COMPONENTS_UPDATE")),
            "STOCK", new AreaPolicy(
                    Set.of("PROJECT_STOCK_READ"),
                    Set.of("PROJECT_STOCK_UPDATE", "STORE_RECEIPT_MANAGE", "STORE_UPDATE")),
            "ELECTRICAL", new AreaPolicy(
                    Set.of("ELECTRICAL_READ"),
                    Set.of("ELECTRICAL_UPDATE")),
            "CONTROLS", new AreaPolicy(
                    Set.of("CONTROLS_TASK_READ"),
                    Set.of("CONTROLS_TASK_MANAGE", "CONTROLS_TASK_UPDATE")),
            "ASSEMBLY", new AreaPolicy(
                    Set.of("ASSEMBLY_READ"),
                    Set.of("ASSEMBLY_UPDATE", "ASSEMBLY_PROGRESS_UPDATE")));

    private ProjectAreaPermissionPolicy() {}

    static boolean supports(String areaCode, String permissionCode, AreaAccessLevel accessLevel) {
        AreaPolicy policy = POLICIES.get(normalize(areaCode));
        if (policy == null) {
            return false;
        }
        String permission = normalize(permissionCode);
        return policy.readPermissions().contains(permission)
                || (accessLevel == AreaAccessLevel.READ_WRITE && policy.writePermissions().contains(permission));
    }

    static boolean hasWritePermission(Set<String> rolePermissions, String areaCode) {
        AreaPolicy policy = POLICIES.get(normalize(areaCode));
        return policy != null && policy.writePermissions().stream().anyMatch(rolePermissions::contains);
    }

    static boolean hasReadPermission(Set<String> rolePermissions, String areaCode) {
        AreaPolicy policy = POLICIES.get(normalize(areaCode));
        return policy != null && policy.readPermissions().stream().anyMatch(rolePermissions::contains);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private record AreaPolicy(Set<String> readPermissions, Set<String> writePermissions) {}
}
