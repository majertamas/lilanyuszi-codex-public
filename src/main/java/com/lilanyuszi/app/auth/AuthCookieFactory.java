package com.lilanyuszi.app.auth;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

import static com.lilanyuszi.app.util.Constant.ACCESS_TOKEN;
import static com.lilanyuszi.app.util.Constant.ACCESS_TOKEN_EXPIRATION_MINUTES;
import static com.lilanyuszi.app.util.Constant.EMPTY_STRING;
import static com.lilanyuszi.app.util.Constant.LAX;
import static com.lilanyuszi.app.util.Constant.REFRESH_TOKEN;
import static com.lilanyuszi.app.util.Constant.REFRESH_TOKEN_EXPIRATION_DAYS;
import static com.lilanyuszi.app.util.Constant.SLASH;

@Component
public class AuthCookieFactory {

    private static final Duration ACCESS_TOKEN_MAX_AGE = Duration.ofMinutes(ACCESS_TOKEN_EXPIRATION_MINUTES);
    private static final Duration REFRESH_TOKEN_MAX_AGE = Duration.ofDays(REFRESH_TOKEN_EXPIRATION_DAYS);

    public ResponseCookie createAccessCookie(String accessToken) {
        return createCookie(ACCESS_TOKEN, accessToken, ACCESS_TOKEN_MAX_AGE);
    }

    public ResponseCookie createRefreshCookie(String refreshToken) {
        return createCookie(REFRESH_TOKEN, refreshToken, REFRESH_TOKEN_MAX_AGE);
    }

    public ResponseCookie deleteAccessCookie() {
        return createCookie(ACCESS_TOKEN, EMPTY_STRING, Duration.ZERO);
    }

    public ResponseCookie deleteRefreshCookie() {
        return createCookie(REFRESH_TOKEN, EMPTY_STRING, Duration.ZERO);
    }

    private ResponseCookie createCookie(String name, String value, Duration maxAge) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(false) // todo true if prod
                .sameSite(LAX)
                .path(SLASH)
                .maxAge(maxAge)
                .build();
    }
}
