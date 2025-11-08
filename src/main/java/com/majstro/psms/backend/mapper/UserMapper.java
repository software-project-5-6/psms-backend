package com.majstro.psms.backend.mapper;

import com.majstro.psms.backend.dto.UserDto;
import com.majstro.psms.backend.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDto toDto(User user) {
        if (user == null) return null;

        return UserDto.builder()
                .id(user.getId())
                .cognitoSub(user.getCognitoSub())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .globalRole(user.getGlobalRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public User toEntity(UserDto dto) {
        if (dto == null) return null;

        return User.builder()
                .id(dto.getId())
                .cognitoSub(dto.getCognitoSub())
                .email(dto.getEmail())
                .fullName(dto.getFullName())
                .globalRole(dto.getGlobalRole())
                .build();
    }
}