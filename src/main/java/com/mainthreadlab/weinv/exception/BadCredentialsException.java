package com.mainthreadlab.weinv.exception;

import com.mainthreadlab.weinv.enums.ErrorKey;

public class BadCredentialsException extends org.springframework.security.authentication.BadCredentialsException {


    private final String keyError;

    public BadCredentialsException(ErrorKey errorKey) {
        super(errorKey.getMessage());
        this.keyError = errorKey.name();
    }

    public BadCredentialsException(ErrorKey errorKey, Throwable cause, String keyError) {
        super(errorKey.getMessage(), cause);
        this.keyError = keyError;
    }

}

