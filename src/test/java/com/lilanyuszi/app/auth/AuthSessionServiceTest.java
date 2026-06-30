package com.lilanyuszi.app.auth;

import com.lilanyuszi.app.api.LilanyusziException;
import com.lilanyuszi.app.token.AccessTokenService;
import com.lilanyuszi.app.token.RefreshRotationResult;
import com.lilanyuszi.app.token.RefreshTokenService;
import com.lilanyuszi.app.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthSessionServiceTest {

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private AccessTokenService accessTokenService;

    @InjectMocks
    private AuthSessionService authSessionService;

    @Test
    void refreshSessionRotatesRefreshTokenAndGeneratesAccessToken() throws LilanyusziException {
        User user = new User();
        RefreshRotationResult rotationResult = new RefreshRotationResult(user, "new-refresh-token");
        when(refreshTokenService.rotateAndGetUser("old-refresh-token")).thenReturn(rotationResult);
        when(accessTokenService.generateAccessToken(user)).thenReturn("new-access-token");

        AuthSession session = authSessionService.refreshSession("old-refresh-token");

        assertEquals("new-access-token", session.accessToken());
        assertEquals("new-refresh-token", session.refreshToken());
    }

    @Test
    void logoutRevokesRefreshTokenWhenPresent() throws LilanyusziException {
        authSessionService.logout("refresh-token");

        verify(refreshTokenService).revoke("refresh-token");
    }

    @Test
    void logoutDoesNothingWhenRefreshTokenIsMissing() throws LilanyusziException {
        authSessionService.logout(null);

        verify(refreshTokenService, never()).revoke(null);
    }
}
