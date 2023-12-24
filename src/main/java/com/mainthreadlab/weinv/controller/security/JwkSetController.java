package com.mainthreadlab.weinv.controller.security;

import com.nimbusds.jose.jwk.JWKSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class JwkSetController {

    private final JWKSet jwkSet;
    private static final String JWK_SET_URI = "/.well-known/jwks.json";

    @GetMapping(JWK_SET_URI)
    public Map<String, Object> getAvailableJwk() {
        log.info("Retrieving available JWK ...");
        return jwkSet.toJSONObject();
    }
}