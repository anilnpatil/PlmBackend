package com.nextfirst.plm.auth.exception;

public class AuthResourceNotFoundException extends RuntimeException {
    public AuthResourceNotFoundException(String resource, Object id) {
        super(resource + " not found: " + id);
    }
}
