package com.mainthreadlab.weinv.exception;

public class ForbiddenException extends RuntimeException {

    public ForbiddenException() {

        super("Forbidden: you don't have permission to access this resource");
    }

}
