package com.mainthreadlab.weinv.controller.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class TokenController {

    private final DefaultTokenServices defaultTokenServices;
    private static final String REVOKE_URI = "/tokens/revoke";

    @PostMapping(REVOKE_URI)
    public void revokeToken(@RequestParam("token") String token) {
        log.info("Revoking access and refresh tokens: {}", token);
        defaultTokenServices.revokeToken(token);
    }
}