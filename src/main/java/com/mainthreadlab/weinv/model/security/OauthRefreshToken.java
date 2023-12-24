package com.mainthreadlab.weinv.model.security;


import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "oauth_refresh_token")
public class OauthRefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String tokenId;

    @Lob
    @Column(columnDefinition = "bigint")
    private String token;

    @Lob
    @Column(columnDefinition = "bigint")
    private String authentication;


}