package com.nextfirst.plm.supplier.mapper;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.nextfirst.plm.supplier.dto.SupplierResponse;
import com.nextfirst.plm.supplier.dto.SupplierUpsertRequest;
import com.nextfirst.plm.supplier.entity.Supplier;

@Component
public class SupplierMapper {

    public Supplier toEntity(SupplierUpsertRequest request) {
        Supplier supplier = new Supplier();
        updateEntity(supplier, request);
        supplier.setActive(true);
        return supplier;
    }

    public void updateEntity(Supplier supplier, SupplierUpsertRequest request) {
        supplier.setSupplierNumber(request.supplierNumber().trim());
        supplier.setName(request.name().trim());
        supplier.setServiceTypes(request.serviceTypes());
        supplier.setContactPerson(trimToNull(request.contactPerson()));
        supplier.setEmail(trimToNull(request.email()));
        supplier.setPhone(trimToNull(request.phone()));
        supplier.setAddress(trimToNull(request.address()));
        supplier.setDescription(trimToNull(request.description()));
    }

    public SupplierResponse toResponse(Supplier supplier) {
        return new SupplierResponse(
                supplier.getId(),
                supplier.getSupplierNumber(),
                supplier.getName(),
                Set.copyOf(supplier.getServiceTypes()),
                supplier.getContactPerson(),
                supplier.getEmail(),
                supplier.getPhone(),
                supplier.getAddress(),
                supplier.getDescription(),
                supplier.isActive(),
                supplier.getCreatedAt(),
                supplier.getUpdatedAt());
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
