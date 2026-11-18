package com.nextfirst.plm.sourcing.service;

import org.springframework.stereotype.Component;

import com.nextfirst.plm.supplier.entity.Supplier;
import com.nextfirst.plm.supplier.entity.SupplierServiceType;
import com.nextfirst.plm.supplier.exception.SupplierNotFoundException;
import com.nextfirst.plm.supplier.repository.SupplierRepository;

@Component
final class SupplierSelectionResolver {

    private final SupplierRepository supplierRepository;

    SupplierSelectionResolver(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    Supplier resolve(Long supplierId, String legacySupplierName, SupplierServiceType serviceType) {
        Supplier supplier;
        if (supplierId != null) {
            supplier = supplierRepository.findById(supplierId)
                    .orElseThrow(() -> new SupplierNotFoundException(supplierId));
        } else if (legacySupplierName != null && !legacySupplierName.isBlank()) {
            supplier = supplierRepository.findByNormalizedName(legacySupplierName.trim())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Select a registered supplier or add it to the supplier directory first"));
        } else {
            return null;
        }

        if (!supplier.isActive()) {
            throw new IllegalArgumentException("Inactive suppliers cannot be assigned to new workflows");
        }
        if (!supplier.getServiceTypes().contains(serviceType)) {
            throw new IllegalArgumentException(
                    "The selected supplier is not configured for " + serviceType.name().toLowerCase() + " services");
        }
        return supplier;
    }
}
