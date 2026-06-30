package com.lilanyuszi.app.user;

public record UserResponse(
        Long id,
        String email,
        String picture,
        String name,
        String nickName
) {
}