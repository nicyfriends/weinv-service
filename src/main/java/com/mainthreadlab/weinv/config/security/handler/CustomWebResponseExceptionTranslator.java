package com.mainthreadlab.weinv.config.security.handler;

import com.mainthreadlab.weinv.exception.security.CustomOAuthException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.provider.error.WebResponseExceptionTranslator;

/**
 * @author nicy malanda
 * @date 2022/11/14
 */
public class CustomWebResponseExceptionTranslator implements WebResponseExceptionTranslator<OAuth2Exception> {

    @Override
    public ResponseEntity<OAuth2Exception> translate(Exception exception) throws Exception {

        if (exception instanceof OAuth2Exception) {
            return ResponseEntity.status(((OAuth2Exception) exception).getHttpErrorCode()).body(new CustomOAuthException(exception.getMessage()));

        } else if (exception instanceof AuthenticationException) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new CustomOAuthException(exception.getMessage()));
        }
        return ResponseEntity.status(HttpStatus.OK).body(new CustomOAuthException(exception.getMessage()));
    }
}
