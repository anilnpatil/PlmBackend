package com.nextfirst.plm.supplier.exception;

public class DuplicateSupplierException extends RuntimeException {

    public DuplicateSupplierException(String field, String value) {
        super("A supplier with " + field + " '" + value + "' already exists");
    }
}
