package com.lilanyuszi.app.auth;

import com.lilanyuszi.app.api.LilanyusziException;
import com.lilanyuszi.app.token.AccessTokenService;
import com.lilanyuszi.app.token.RefreshRotationResult;
import com.lilanyuszi.app.token.RefreshTokenService;
import com.lilanyuszi.app.user.User;
import com.lilanyuszi.app.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthSessionService {

    private final RefreshTokenService refreshTokenService;
    private final AccessTokenService accessTokenService;
    private final UserService userService;

    public AuthSession refreshSession(String refreshToken) throws LilanyusziException {
        RefreshRotationResult result = refreshTokenService.rotateAndGetUser(refreshToken);
        String accessToken = accessTokenService.generateAccessToken(result.user());

        return new AuthSession(accessToken, result.refreshToken());
    }

    public void logout(String refreshToken) throws LilanyusziException {
        if (refreshToken != null) {
            refreshTokenService.revoke(refreshToken);
        }
    }

    public AuthSession createOAuthSession(OidcUser oidcUser) throws LilanyusziException {
        User user = userService.findOrCreateOAuthUser(oidcUser);
        String accessToken = accessTokenService.generateAccessToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user);
        return new AuthSession(accessToken, refreshToken);
    }
}
