package com.mainthreadlab.weinv.model.security;


import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "oauth_access_token")
public class OauthAccessToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String tokenId;

    @Lob
    @Column(columnDefinition = "bigint")
    private String token;

    private String authenticationId;

    private String username;

    private String clientId;

    @Lob
    @Column(columnDefinition = "bigint")
    private String authentication;

    private String refreshToken;


}
