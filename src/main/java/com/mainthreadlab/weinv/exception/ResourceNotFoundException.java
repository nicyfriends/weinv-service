package com.mainthreadlab.weinv.exception;

import com.mainthreadlab.weinv.enums.ErrorKey;
import lombok.Getter;

@Getter
public class ResourceNotFoundException extends RuntimeException {

    private final String keyError;
//
//    public ResourceNotFoundException(String className, String uuid) {
//        this.message = String.format("Not found resource %s, uuid = %s", className, uuid);
//    }

    public ResourceNotFoundException(ErrorKey errorKey) {
        super(errorKey.getMessage());
        this.keyError = errorKey.name();
    }
}
