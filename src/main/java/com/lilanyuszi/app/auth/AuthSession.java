package com.lilanyuszi.app.auth;

public record AuthSession(
        String accessToken,
        String refreshToken
) {
}
