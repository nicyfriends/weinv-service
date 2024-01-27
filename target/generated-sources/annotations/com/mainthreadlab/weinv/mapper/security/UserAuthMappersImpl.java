package com.mainthreadlab.weinv.mapper.security;

import com.mainthreadlab.weinv.dto.security.AuthUserRequest;
import com.mainthreadlab.weinv.model.security.UserAuth;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-01-27T18:29:16+0100",
    comments = "version: 1.4.2.Final, compiler: javac, environment: Java 20 (Oracle Corporation)"
)
@Component
public class UserAuthMappersImpl implements UserAuthMappers {

    @Override
    public UserAuth map(AuthUserRequest authUserRequest) {
        if ( authUserRequest == null ) {
            return null;
        }

        UserAuth userAuth = new UserAuth();

        userAuth.setEmail( authUserRequest.getEmail() );
        userAuth.setUsername( authUserRequest.getUsername() );
        userAuth.setPassword( authUserRequest.getPassword() );

        return userAuth;
    }
}
