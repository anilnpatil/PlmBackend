package com.nextfirst.plm.supplier.dto;

import jakarta.validation.constraints.NotNull;

public record SupplierStatusRequest(@NotNull Boolean active) {
}
