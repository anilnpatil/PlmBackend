package com.nextfirst.plm.project.exception;

public class DuplicateProjectNumberException extends RuntimeException {

    public DuplicateProjectNumberException(String projectNumber) {
        super("Project number '" + projectNumber + "' is already in use");
    }
}
