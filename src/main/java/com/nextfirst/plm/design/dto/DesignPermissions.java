package com.nextfirst.plm.design.dto;

public record DesignPermissions(
        boolean canCreateAssembly,
        boolean canUpdateAssemblies,
        boolean canCreatePart,
        boolean canReadParts,
        boolean canUpdateParts) {
}
