package com.mainthreadlab.weinv.repository.security;

import com.mainthreadlab.weinv.model.security.OauthAccessToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomAccessTokenRepository extends JpaRepository<OauthAccessToken, Integer> {
    Optional<OauthAccessToken> findByTokenId(String tokenId);

    Optional<OauthAccessToken> findByRefreshToken(String tokenId);

    List<OauthAccessToken> findByAuthenticationId(String authenticationId);

    List<OauthAccessToken> findByClientIdAndUsername(String clientId, String userName);

    List<OauthAccessToken> findByClientId(String clientId);
}
