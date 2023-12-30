package com.mainthreadlab.weinv.commons.security;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

@Slf4j
public class Serialization {


    private Serialization() {
    }


    public static String serialize(Object object) {
        try {
            byte[] bytes = org.springframework.security.oauth2.common.util.SerializationUtils.serialize(object);
            return Base64.encodeBase64String(bytes);
        } catch (Throwable e) {
            log.error("Error: {}", e.getMessage());
            throw e;
        }
    }

    public static OAuth2AccessToken deserializeAccessToken(String encodedObject) {
        try {
            byte[] bytes = Base64.decodeBase64(encodedObject);
            return org.springframework.security.oauth2.common.util.SerializationUtils.deserialize(bytes);
        } catch (Throwable e) {
            log.error("Error: {}", e.getMessage());
            throw e;
        }
    }

    public static OAuth2RefreshToken deserializeRefreshToken(String encodedObject) {
        try {
            byte[] bytes = Base64.decodeBase64(encodedObject);
            return org.springframework.security.oauth2.common.util.SerializationUtils.deserialize(bytes);
        } catch (Throwable e) {
            log.error("Error: {}", e.getMessage());
            throw e;
        }
    }

    public static OAuth2Authentication deserializeAuthentication(String encodedObject) {
        try {
            byte[] bytes = Base64.decodeBase64(encodedObject);
            return org.springframework.security.oauth2.common.util.SerializationUtils.deserialize(bytes);
        } catch (Throwable e) {
            log.error("Error: {}", e.getMessage());
            throw e;
        }
    }

}
