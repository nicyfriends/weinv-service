package com.mainthreadlab.weinv.exception;

import com.mainthreadlab.weinv.enums.ErrorKey;
import lombok.Getter;

@Getter
public class InternalServerError extends RuntimeException {

    private final String keyError;

    public InternalServerError(ErrorKey errorKey) {
        super(errorKey.getMessage());
        this.keyError = errorKey.name();
    }

    public InternalServerError(ErrorKey errorKey, Throwable cause) {
        super(errorKey.getMessage(), cause);
        this.keyError = errorKey.name();
    }

}

