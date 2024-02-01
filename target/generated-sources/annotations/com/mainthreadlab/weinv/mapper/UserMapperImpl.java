package com.mainthreadlab.weinv.mapper;

import com.mainthreadlab.weinv.dto.request.UpdateUserRequest;
import com.mainthreadlab.weinv.dto.security.AuthUpdateUserRequest;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-02-01T17:16:33+0100",
    comments = "version: 1.4.2.Final, compiler: javac, environment: Java 20 (Oracle Corporation)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public AuthUpdateUserRequest map(UpdateUserRequest updateUserRequest) {
        if ( updateUserRequest == null ) {
            return null;
        }

        AuthUpdateUserRequest authUpdateUserRequest = new AuthUpdateUserRequest();

        authUpdateUserRequest.setEmail( mapRole( updateUserRequest.getEmail() ) );
        authUpdateUserRequest.setCurrentPassword( mapRole( updateUserRequest.getCurrentPassword() ) );
        authUpdateUserRequest.setNewPassword( mapRole( updateUserRequest.getNewPassword() ) );

        return authUpdateUserRequest;
    }
}
