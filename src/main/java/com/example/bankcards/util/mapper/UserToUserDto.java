package com.example.bankcards.util.mapper;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.User;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("userToUserDto")
public class UserToUserDto implements DtoMapper<UserDto, User> {
    @Override
    public UserDto map(User dto) {
        return UserDto.builder()
                .email(dto.getEmail())
                .userId(dto.getUserId())
                .notificationTypes(dto.getNotificationTypes())
                .role(dto.getRole())
                .roleName(dto.getRole().getRoleName())
                .build();
    }
}
