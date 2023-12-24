package com.mainthreadlab.weinv.config;

import com.mainthreadlab.weinv.dto.security.CustomUserDetails;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.util.JsonParser;
import org.springframework.security.oauth2.common.util.JsonParserFactory;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class JwtCustomHeadersAccessTokenConverter extends JwtAccessTokenConverter {

    private final Map<String, String> customHeaders;
    private final JsonParser objectMapper = JsonParserFactory.create();
    final RsaSigner signer;

    public JwtCustomHeadersAccessTokenConverter(Map<String, String> customHeaders, KeyPair keyPair) {
        super();
        super.setKeyPair(keyPair);
        this.signer = new RsaSigner((RSAPrivateKey) keyPair.getPrivate());
        this.customHeaders = customHeaders;
    }

    @Override
    protected String encode(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
        String content;
        try {
            content = this.objectMapper.formatMap(getAccessTokenConverter().convertAccessToken(accessToken, authentication));
        } catch (Throwable e) {
            throw new IllegalStateException("Cannot convert access token to JSON", e);
        }
        return JwtHelper.encode(content, this.signer, this.customHeaders).getEncoded();
    }

    /**
     * Add custom user principal information to the JWT token
     */
    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
        if ("password".equals(authentication.getOAuth2Request().getGrantType())) {
            CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();

            Map<String, Object> additionalInfo = new LinkedHashMap<>(accessToken.getAdditionalInformation());

            additionalInfo.put("created_at", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
            additionalInfo.put("expiredAt", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(accessToken.getExpiresIn()));
            additionalInfo.put("email", user.getEmail());
            additionalInfo.put("client_id", authentication.getOAuth2Request().getClientId());

            DefaultOAuth2AccessToken customAccessToken = new DefaultOAuth2AccessToken(accessToken);
            customAccessToken.setAdditionalInformation(additionalInfo);
            return super.enhance(customAccessToken, authentication);

        } else {
            return super.enhance(accessToken, authentication);
        }
    }

}
