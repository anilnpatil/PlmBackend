package com.nextfirst.plm.supplier.dto;

import java.time.Instant;
import java.util.Set;

import com.nextfirst.plm.supplier.entity.SupplierServiceType;

public record SupplierResponse(
        Long id,
        String supplierNumber,
        String name,
        Set<SupplierServiceType> serviceTypes,
        String contactPerson,
        String email,
        String phone,
        String address,
        String description,
        boolean active,
        Instant createdAt,
        Instant updatedAt) {
}
