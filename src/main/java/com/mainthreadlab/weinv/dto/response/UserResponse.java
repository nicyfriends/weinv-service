package com.mainthreadlab.weinv.dto.response;

import com.mainthreadlab.weinv.model.enums.EventType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class UserResponse {

    private String uuid;
    private String username;
    private String lastName;
    private String firstName;
    private String wife;
    private String husband;
    private String phoneNumber;
    private String email;
    private List<String> roles;
    private Integer tableNumber;
    private String language;
    private EventType eventType;
}
