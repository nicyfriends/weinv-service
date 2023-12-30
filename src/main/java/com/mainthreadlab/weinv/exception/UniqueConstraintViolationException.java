package com.mainthreadlab.weinv.exception;

import com.mainthreadlab.weinv.model.enums.ErrorKey;
import lombok.Getter;

@Getter
public class UniqueConstraintViolationException extends RuntimeException {

    private final String keyError;

    public UniqueConstraintViolationException(ErrorKey keyError) {
        super(keyError.getMessage());
        this.keyError = keyError.name();
    }
}
