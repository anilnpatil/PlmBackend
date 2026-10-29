package com.nextfirst.plm.design.dto;

import java.time.Instant;
import java.util.List;

public record DesignSubAssemblyResponse(
        Long id,
        Long mainAssemblyId,
        String assemblyNumber,
        String name,
        String description,
        String createdBy,
        Instant createdAt,
        List<DesignPartsResponse> parts) {
}
