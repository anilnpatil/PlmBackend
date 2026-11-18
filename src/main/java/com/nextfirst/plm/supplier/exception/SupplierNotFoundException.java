package com.nextfirst.plm.supplier.exception;

public class SupplierNotFoundException extends RuntimeException {

    public SupplierNotFoundException(Long id) {
        super("Supplier with id " + id + " was not found");
    }
}
