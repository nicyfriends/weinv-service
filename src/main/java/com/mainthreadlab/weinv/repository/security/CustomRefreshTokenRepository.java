package com.mainthreadlab.weinv.repository.security;

import com.mainthreadlab.weinv.model.security.OauthRefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomRefreshTokenRepository extends JpaRepository<OauthRefreshToken, Integer> {
    Optional<OauthRefreshToken> findByTokenId(String tokenId);
}
