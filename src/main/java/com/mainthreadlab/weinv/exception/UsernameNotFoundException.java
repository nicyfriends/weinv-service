package com.mainthreadlab.weinv.exception;

import com.mainthreadlab.weinv.model.enums.ErrorKey;

public class UsernameNotFoundException extends org.springframework.security.core.userdetails.UsernameNotFoundException {

    private final String keyError;

    public UsernameNotFoundException(ErrorKey errorKey) {
        super(errorKey.getMessage());
        this.keyError = errorKey.name();
    }

    public UsernameNotFoundException(ErrorKey errorKey, Throwable cause, String keyError) {
        super(errorKey.getMessage(), cause);
        this.keyError = keyError;
    }

}

