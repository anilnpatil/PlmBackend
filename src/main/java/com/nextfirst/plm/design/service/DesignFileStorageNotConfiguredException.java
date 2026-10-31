package com.nextfirst.plm.design.service;

public class DesignFileStorageNotConfiguredException extends RuntimeException {
    public DesignFileStorageNotConfiguredException() {
        super("Server-side design file access is not configured");
    }
}
