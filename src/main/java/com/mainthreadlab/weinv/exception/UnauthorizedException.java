package com.mainthreadlab.weinv.exception;

import com.mainthreadlab.weinv.enums.ErrorKey;
import lombok.Getter;

@Getter
public class UnauthorizedException extends RuntimeException {

    private final String keyError;

    public UnauthorizedException(ErrorKey keyError) {
        super(keyError.getMessage());
        this.keyError = keyError.name();
    }

}
