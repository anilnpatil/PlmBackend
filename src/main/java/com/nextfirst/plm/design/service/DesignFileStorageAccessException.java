package com.nextfirst.plm.design.service;

public class DesignFileStorageAccessException extends RuntimeException {
    public DesignFileStorageAccessException() {
        super("Unable to access the registered server design-file folder");
    }
}
