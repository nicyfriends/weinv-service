package com.mainthreadlab.weinv.exception;

import com.mainthreadlab.weinv.model.enums.ErrorKey;
import lombok.Getter;

@Getter
public class BadRequestException extends RuntimeException {

    private final String keyError;

    public BadRequestException(ErrorKey errorKey) {
        super(errorKey.getMessage());
        this.keyError = errorKey.name();
    }

    public BadRequestException(ErrorKey errorKey, Throwable cause, String keyError) {
        super(errorKey.getMessage(), cause);
        this.keyError = keyError;
    }

}

