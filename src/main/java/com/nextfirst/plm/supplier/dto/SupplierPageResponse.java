package com.nextfirst.plm.supplier.dto;

import java.util.List;

public record SupplierPageResponse(
        List<SupplierResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last) {
}
