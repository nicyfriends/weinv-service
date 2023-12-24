package com.mainthreadlab.weinv.service.security;

import com.mainthreadlab.weinv.model.security.OauthAccessToken;
import com.mainthreadlab.weinv.model.security.OauthRefreshToken;
import com.mainthreadlab.weinv.repository.security.CustomAccessTokenRepository;
import com.mainthreadlab.weinv.repository.security.CustomRefreshTokenRepository;
import com.mainthreadlab.weinv.util.security.SerializationUtils;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.DefaultAuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Transactional
public class CustomJwtTokenStore extends JwtTokenStore {

    private final CustomAccessTokenRepository accessTokenRepository;
    private final CustomRefreshTokenRepository refreshTokenRepository;
    private final AuthenticationKeyGenerator authenticationKeyGenerator;


    /**
     * Create a JwtTokenStore with this token enhancer (should be shared with the DefaultTokenServices if used).
     *
     * @param jwtTokenEnhancer
     */
    public CustomJwtTokenStore(JwtAccessTokenConverter jwtTokenEnhancer, CustomAccessTokenRepository accessTokenRepository, CustomRefreshTokenRepository refreshTokenRepository) {
        super(jwtTokenEnhancer);
        this.accessTokenRepository = accessTokenRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.authenticationKeyGenerator = new DefaultAuthenticationKeyGenerator();
    }

    @Override
    public OAuth2Authentication readAuthentication(OAuth2AccessToken accessToken) {
        return readAuthentication(accessToken.getValue());
    }

    @Override
    public OAuth2Authentication readAuthentication(String token) {
        Optional<OauthAccessToken> accessToken = accessTokenRepository.findByTokenId(extractTokenKey(token));
        return accessToken.map(op -> SerializationUtils.deserializeAuthentication(op.getAuthentication())).orElse(null);
    }

    @Override
    public void storeAccessToken(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
        String refreshToken = null;
        if (accessToken.getRefreshToken() != null) {
            refreshToken = accessToken.getRefreshToken().getValue();
        }

        if (readAccessToken(accessToken.getValue()) != null) {
            this.removeAccessToken(accessToken);
        }

        OauthAccessToken oauthAccessToken = new OauthAccessToken();
        oauthAccessToken.setTokenId(extractTokenKey(accessToken.getValue()));
        oauthAccessToken.setToken(SerializationUtils.serialize(accessToken));
        oauthAccessToken.setAuthenticationId(authenticationKeyGenerator.extractKey(authentication));
        oauthAccessToken.setUsername(authentication.isClientOnly() ? null : authentication.getName());
        oauthAccessToken.setClientId(authentication.getOAuth2Request().getClientId());
        oauthAccessToken.setAuthentication(SerializationUtils.serialize(authentication));
        oauthAccessToken.setRefreshToken(extractTokenKey(refreshToken));

        accessTokenRepository.save(oauthAccessToken);
    }

    @Override
    public OAuth2AccessToken readAccessToken(String tokenValue) {
        Optional<OauthAccessToken> accessToken = accessTokenRepository.findByTokenId(extractTokenKey(tokenValue));
        return accessToken.map(op -> SerializationUtils.deserializeAccessToken(op.getToken())).orElse(null);
    }

    @Override
    public void removeAccessToken(OAuth2AccessToken token) {
        Optional<OauthAccessToken> accessToken = accessTokenRepository.findByTokenId(extractTokenKey(token.getValue()));
        accessToken.ifPresent(accessTokenRepository::delete);
    }

    @Override
    public void storeRefreshToken(OAuth2RefreshToken refreshToken, OAuth2Authentication authentication) {
        OauthRefreshToken oauthRefreshToken = new OauthRefreshToken();
        oauthRefreshToken.setTokenId(extractTokenKey(refreshToken.getValue()));
        oauthRefreshToken.setToken(SerializationUtils.serialize(refreshToken));
        oauthRefreshToken.setAuthentication(SerializationUtils.serialize(authentication));
        refreshTokenRepository.save(oauthRefreshToken);
    }

    @Override
    public OAuth2RefreshToken readRefreshToken(String tokenValue) {
        Optional<OauthRefreshToken> refreshToken = refreshTokenRepository.findByTokenId(extractTokenKey(tokenValue));
        return refreshToken.map(op -> SerializationUtils.deserializeRefreshToken(op.getToken())).orElse(null);
    }

    @Override
    public OAuth2Authentication readAuthenticationForRefreshToken(OAuth2RefreshToken refreshToken) {
        Optional<OauthRefreshToken> oauthRefreshToken = refreshTokenRepository.findByTokenId(extractTokenKey(refreshToken.getValue()));
        return oauthRefreshToken.map(op -> SerializationUtils.deserializeAuthentication(op.getAuthentication())).orElse(null);
    }

    @Override
    public void removeRefreshToken(OAuth2RefreshToken token) {
        Optional<OauthRefreshToken> oauthRefreshToken = refreshTokenRepository.findByTokenId(extractTokenKey(token.getValue()));
        oauthRefreshToken.ifPresent(refreshTokenRepository::delete);
    }

    @Override
    public void removeAccessTokenUsingRefreshToken(OAuth2RefreshToken refreshToken) {
        Optional<OauthAccessToken> accessToken = accessTokenRepository.findByRefreshToken(extractTokenKey(refreshToken.getValue()));
        accessToken.ifPresent(accessTokenRepository::delete);
    }

    @Override
    public OAuth2AccessToken getAccessToken(OAuth2Authentication authentication) {
        OAuth2AccessToken accessToken = null;
        String authenticationId = authenticationKeyGenerator.extractKey(authentication);
        List<OauthAccessToken> accessTokens = accessTokenRepository.findByAuthenticationId(authenticationId);
        OauthAccessToken token = accessTokens.isEmpty() ? null : DataAccessUtils.nullableSingleResult(accessTokens);

        if (token != null) {
            accessToken = SerializationUtils.deserializeAccessToken(token.getToken());
            if (accessToken != null && !authenticationId.equals(authenticationKeyGenerator.extractKey(this.readAuthentication(accessToken)))) {
                this.removeAccessToken(accessToken);
                this.storeAccessToken(accessToken, authentication);
            }
        }
        return accessToken;
    }

    @Override
    public Collection<OAuth2AccessToken> findTokensByClientIdAndUserName(String clientId, String userName) {
        Collection<OAuth2AccessToken> tokens = new ArrayList<>();
        List<OauthAccessToken> result = accessTokenRepository.findByClientIdAndUsername(clientId, userName);
        result.forEach(e -> tokens.add(SerializationUtils.deserializeAccessToken(e.getToken())));
        return tokens;
    }

    @Override
    public Collection<OAuth2AccessToken> findTokensByClientId(String clientId) {
        Collection<OAuth2AccessToken> tokens = new ArrayList<>();
        List<OauthAccessToken> result = accessTokenRepository.findByClientId(clientId);
        result.forEach(e -> tokens.add(SerializationUtils.deserializeAccessToken(e.getToken())));
        return tokens;
    }

    String extractTokenKey(String value) {
        if (value == null) {
            return null;
        }
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 algorithm not available.  Fatal (should be in the JDK).");
        }

        byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
        return String.format("%032x", new BigInteger(1, bytes));
    }
}
