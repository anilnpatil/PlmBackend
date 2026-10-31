package com.nextfirst.plm.design.dto;

public record DesignPermissions(
        boolean canCreateAssembly,
        boolean canCreatePart,
        boolean canReadParts,
        boolean canUpdateParts) {
}
