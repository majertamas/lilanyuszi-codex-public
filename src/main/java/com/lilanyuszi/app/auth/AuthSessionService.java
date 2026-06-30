package com.lilanyuszi.app.auth;

import com.lilanyuszi.app.token.AccessTokenService;
import com.lilanyuszi.app.token.RefreshRotationResult;
import com.lilanyuszi.app.token.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthSessionService {

    private final RefreshTokenService refreshTokenService;
    private final AccessTokenService accessTokenService;

    public AuthSession refreshSession(String refreshToken) {
        RefreshRotationResult result = refreshTokenService.rotateAndGetUser(refreshToken);
        String accessToken = accessTokenService.generateAccessToken(result.user());

        return new AuthSession(accessToken, result.refreshToken());
    }

    public void logout(String refreshToken) {
        if (refreshToken != null) {
            refreshTokenService.revoke(refreshToken);
        }
    }
}
