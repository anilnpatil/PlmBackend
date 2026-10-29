package com.nextfirst.plm.design.dto;

import java.util.List;

public record DesignWorkspaceResponse(
        List<DesignMainAssemblyResponse> mainAssemblies,
        DesignPermissions permissions) {
}
