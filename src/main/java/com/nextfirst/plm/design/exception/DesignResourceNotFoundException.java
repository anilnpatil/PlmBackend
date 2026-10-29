package com.nextfirst.plm.design.exception;

import com.nextfirst.plm.auth.exception.AuthResourceNotFoundException;

public class DesignResourceNotFoundException extends AuthResourceNotFoundException {
    public DesignResourceNotFoundException(String resource, Object id) {
        super(resource, id);
    }
}
