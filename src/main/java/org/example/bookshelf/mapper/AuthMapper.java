package org.example.bookshelf.mapper;

import org.example.bookshelf.dto.auth.AuthResponse;
import org.example.bookshelf.entity.User;

public class AuthMapper {
    public static AuthResponse toResponse(User user) {
        return AuthResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .build();
    }
}
