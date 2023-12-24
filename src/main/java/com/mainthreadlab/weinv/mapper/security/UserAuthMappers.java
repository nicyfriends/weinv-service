package com.mainthreadlab.weinv.mapper.security;

import com.mainthreadlab.weinv.model.security.UserAuth;
import com.mainthreadlab.weinv.dto.security.AuthUserRequest;
import org.mapstruct.Mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Mapper
public interface UserAuthMappers {

    UserAuth map(AuthUserRequest authUserRequest);

    default Map<String, Object> mapAdditionalInfos(String additionalInfos) {
        Map<String, Object> infos = new HashMap<>();
        if (additionalInfos != null) {
            List.of(additionalInfos.split(",")).forEach(i -> infos.put(i, i));
        }
        return infos;
    }


}
