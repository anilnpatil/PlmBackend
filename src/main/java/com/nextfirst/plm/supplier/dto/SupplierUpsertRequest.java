package com.nextfirst.plm.supplier.dto;

import java.util.Set;

import com.nextfirst.plm.supplier.entity.SupplierServiceType;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SupplierUpsertRequest(
        @NotBlank @Size(max = 30) String supplierNumber,
        @NotBlank @Size(max = 150) String name,
        @NotEmpty Set<@NotNull SupplierServiceType> serviceTypes,
        @Size(max = 120) String contactPerson,
        @Email @Size(max = 254) String email,
        @Size(max = 40) String phone,
        @Size(max = 500) String address,
        @Size(max = 2000) String description) {
}
