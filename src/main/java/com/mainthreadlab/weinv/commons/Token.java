package com.mainthreadlab.weinv.commons;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mainthreadlab.weinv.config.security.annotation.JwtDetails;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;

public class Token {

    private Token() {
    }

    public static JwtDetails getJwtDetails(String header) throws ParseException {
        if (StringUtils.isNotBlank(header) && header.contains(Constants.BEARER)) {
            String accessToken = header.replace(Constants.BEARER, "");
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
