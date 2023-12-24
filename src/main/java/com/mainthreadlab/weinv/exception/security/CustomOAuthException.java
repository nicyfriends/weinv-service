package com.mainthreadlab.weinv.exception.security;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;

/**
 * @author nicy malanda
 * @date 2022/11/14
 */
@JsonSerialize(using = CustomOAuthExceptionSerializer.class)
public class CustomOAuthException extends OAuth2Exception {

    public CustomOAuthException(String msg) {
        super(msg);
    }

}