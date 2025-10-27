package com.example.bankcards.util.mapper;

import com.example.bankcards.dto.UserAuthDto;
import com.example.bankcards.entity.User;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("userAuthMap")
public class UserAuthMapper implements DtoMapper<User, UserAuthDto> {
    @Override
    public User map(UserAuthDto dto) {
        return User.builder().
                email(dto.getEmail()).
                password(dto.getPassword()).
                build();
    }
}
