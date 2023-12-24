package com.mainthreadlab.weinv.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mainthreadlab.weinv.config.security.annotation.JwtDetails;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;

public class TokenUtils {

    private TokenUtils() {
    }

    public static JwtDetails getJwtDetails(String header) throws ParseException {
        if (StringUtils.isNotBlank(header) && header.contains("Bearer ")) {
            String accessToken = header.replace("Bearer ", "");
            JWT jwt = JWTParser.parse(accessToken);
            JWTClaimsSet jwtClaimSet = jwt.getJWTClaimsSet();
            ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            JwtDetails jwtDetails = objectMapper.convertValue(jwtClaimSet.getClaims(), JwtDetails.class);
            jwtDetails.setToken(accessToken);
            return jwtDetails;
        } else {
            return null;
        }
    }

}
