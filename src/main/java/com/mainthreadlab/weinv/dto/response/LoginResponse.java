package com.mainthreadlab.weinv.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mainthreadlab.weinv.model.enums.EventType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class LoginResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken;

    private String uuidUser;

    private String uuidWedding;    // for guest/responsible

    private List<String> roles;
    private String firstName;

    private String lastName;

    private EventType eventType;

}
