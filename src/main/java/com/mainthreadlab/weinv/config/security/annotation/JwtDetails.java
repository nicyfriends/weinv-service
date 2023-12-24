package com.mainthreadlab.weinv.config.security.annotation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

import java.sql.Timestamp;
import java.util.List;

@Data
public class JwtDetails {

    @JsonProperty("aud")
    private List<String> resources;

    @JsonProperty("user_name")
    private String username;

    @JsonProperty("scope")
    private List<String> scopes;

    @JsonProperty("created_at")
    @Getter(AccessLevel.NONE)
    private String createdAt;

    @JsonProperty("exp")
    @Getter(AccessLevel.NONE)
    private String expirationTime;

    private List<String> authorities;

    private String email;

    private String token;

    @JsonProperty("client_id")
    private String clientId;


    public Timestamp expireAt() {
        return Timestamp.valueOf(expirationTime);
    }

    public Timestamp createdAt() {
        return Timestamp.valueOf(createdAt);
    }

}
