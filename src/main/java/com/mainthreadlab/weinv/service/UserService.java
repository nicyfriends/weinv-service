package com.mainthreadlab.weinv.service;

import com.mainthreadlab.weinv.config.security.annotation.JwtDetails;
import com.mainthreadlab.weinv.model.User;
import com.mainthreadlab.weinv.dto.request.CredentialsRequest;
import com.mainthreadlab.weinv.dto.request.UpdateUserRequest;
import com.mainthreadlab.weinv.dto.request.UserRequest;
import com.mainthreadlab.weinv.dto.response.LoginResponse;
import com.mainthreadlab.weinv.dto.response.UserResponse;

import java.io.IOException;
import java.net.URISyntaxException;

public interface UserService {

    LoginResponse login(CredentialsRequest credentialsRequest) throws URISyntaxException, IOException;

    String registerEventResponsible(JwtDetails jwtDetails, UserRequest userRequest);

    User save(UserRequest userRequest);

    void deleteGuestInvitation(String uuid, String uuidEvent);

    UserResponse getUser(String uuid);

    User getByUuid(String responsibleUUID);

    void updateUser(String uuid, String uuidEvent, UpdateUserRequest updateUserRequest) throws URISyntaxException;

}
