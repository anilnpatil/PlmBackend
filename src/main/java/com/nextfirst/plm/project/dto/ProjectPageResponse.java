package com.nextfirst.plm.project.dto;

import java.util.List;

public record ProjectPageResponse(
        List<ProjectResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last) {
}
