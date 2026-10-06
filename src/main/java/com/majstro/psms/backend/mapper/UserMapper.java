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
                .authSub(user.getAuthSub())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .globalRole(user.getGlobalRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .gmailAccessToken(user.getGmailAccessToken())
                .gmailRefreshToken(user.getGmailRefreshToken())
                .zoomAccessToken(user.getZoomAccessToken())
                .zoomRefreshToken(user.getZoomRefreshToken())
                .build();
    }

    public User toEntity(UserDto dto) {
        if (dto == null) return null;

        return User.builder()
                .id(dto.getId())
                .authSub(dto.getAuthSub())
                .email(dto.getEmail())
                .fullName(dto.getFullName())
                .globalRole(dto.getGlobalRole())
                .build();
    }
}
