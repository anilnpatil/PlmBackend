package com.nextfirst.plm.design.dto;

import java.time.Instant;
import java.util.List;

public record DesignMainAssemblyResponse(
        Long id,
        Long projectId,
        String assemblyNumber,
        String name,
        String description,
        String createdBy,
        Instant createdAt,
        List<DesignPartsResponse> parts,
        List<DesignSubAssemblyResponse> subAssemblies) {
}
