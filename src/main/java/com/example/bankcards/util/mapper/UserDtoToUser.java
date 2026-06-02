package com.example.bankcards.util.mapper;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.User;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("userDtoToUser")
public class UserDtoToUser implements DtoMapper<User, UserDto> {
    @Override
    public User map(UserDto dto) {
        return User.builder()
                .email(dto.getEmail())
                .password(dto.getPassword())
                .userId(dto.getUserId())
                .notificationTypes(dto.getNotificationTypes())
                .role(dto.getRole())
                .build();
    }
}
