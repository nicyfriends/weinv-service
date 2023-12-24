package com.mainthreadlab.weinv.model.security;


import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;

@Data
@Entity
@NoArgsConstructor
@Table(name = "oauth_client_details")
public class OauthClientDetails {

    @Id
    private String clientId;

    @Column(nullable = false)
    private String clientSecret;

    private String resourceIds;

    @Column(nullable = false)
    private String scope;

    private String authorizedGrantTypes;

    private String webServerRedirectUri;

    @Column(nullable = false)
    private String authorities;

    private Integer accessTokenValidity;

    private Integer refreshTokenValidity;

    @Column(columnDefinition = "boolean default true")
    private Boolean autoapprove;

    private String additionalInformation;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OauthClientDetails that = (OauthClientDetails) o;
        return clientId.equals(that.clientId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientId);
    }
}