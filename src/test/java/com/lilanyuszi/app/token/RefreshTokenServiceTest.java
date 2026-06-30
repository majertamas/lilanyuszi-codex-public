package com.lilanyuszi.app.token;

import com.lilanyuszi.app.api.LilanyusziException;
import com.lilanyuszi.app.user.User;
import com.lilanyuszi.app.user.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    private static final String RAW_TOKEN = "raw-token";

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Test
    void createRefreshTokenSavesHashedTokenAndReturnsRawToken() throws LilanyusziException {
        User user = user();

        String rawToken = refreshTokenService.createRefreshToken(user);

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());

        RefreshToken savedToken = captor.getValue();
        assertNotNull(rawToken);
        assertSame(user, savedToken.getUser());
        assertEquals(refreshTokenService.hash(rawToken), savedToken.getTokenHash());
        assertNotEquals(rawToken, savedToken.getTokenHash());
        assertNotNull(savedToken.getCreatedAt());
        assertNotNull(savedToken.getExpiresAt());
        assertTrue(savedToken.getExpiresAt().isAfter(savedToken.getCreatedAt()));
    }

    @Test
    void rotateAndGetUserRevokesCurrentTokenAndCreatesNewToken() throws LilanyusziException {
        User user = user();
        RefreshToken currentToken = refreshToken(user, Instant.now().plusSeconds(60), null);
        when(refreshTokenRepository.findByTokenHash(refreshTokenService.hash(RAW_TOKEN)))
                .thenReturn(Optional.of(currentToken));

        RefreshRotationResult result = refreshTokenService.rotateAndGetUser(RAW_TOKEN);

        assertSame(user, result.user());
        assertNotNull(result.refreshToken());
        assertNotNull(currentToken.getRevokedAt());
        verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class));
    }

    @Test
    void rotateAndGetUserThrowsWhenTokenDoesNotExist() throws LilanyusziException {
        when(refreshTokenRepository.findByTokenHash(refreshTokenService.hash(RAW_TOKEN)))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> refreshTokenService.rotateAndGetUser(RAW_TOKEN)
        );

        assertEquals("Invalid refresh token", exception.getMessage());
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void rotateAndGetUserThrowsWhenTokenIsRevoked() throws LilanyusziException {
        RefreshToken currentToken = refreshToken(user(), Instant.now().plusSeconds(60), Instant.now());
        when(refreshTokenRepository.findByTokenHash(refreshTokenService.hash(RAW_TOKEN)))
                .thenReturn(Optional.of(currentToken));

        LilanyusziException exception = assertThrows(
                LilanyusziException.class,
                () -> refreshTokenService.rotateAndGetUser(RAW_TOKEN)
        );

        assertEquals("Refresh token revoked", exception.getExMessage().getText());
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void rotateAndGetUserThrowsWhenTokenIsExpired() throws LilanyusziException {
        RefreshToken currentToken = refreshToken(user(), Instant.now().minusSeconds(60), null);
        when(refreshTokenRepository.findByTokenHash(refreshTokenService.hash(RAW_TOKEN)))
                .thenReturn(Optional.of(currentToken));

        LilanyusziException exception = assertThrows(
                LilanyusziException.class,
                () -> refreshTokenService.rotateAndGetUser(RAW_TOKEN)
        );

        assertEquals("Refresh token expired", exception.getExMessage().getText());
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void revokeMarksTokenAsRevokedWhenTokenExists() throws LilanyusziException {
        RefreshToken refreshToken = refreshToken(user(), Instant.now().plusSeconds(60), null);
        when(refreshTokenRepository.findByTokenHash(refreshTokenService.hash(RAW_TOKEN)))
                .thenReturn(Optional.of(refreshToken));

        refreshTokenService.revoke(RAW_TOKEN);

        assertNotNull(refreshToken.getRevokedAt());
        verify(refreshTokenRepository).save(refreshToken);
    }

    @Test
    void revokeDoesNothingWhenTokenDoesNotExist() throws LilanyusziException {
        when(refreshTokenRepository.findByTokenHash(refreshTokenService.hash(RAW_TOKEN)))
                .thenReturn(Optional.empty());

        refreshTokenService.revoke(RAW_TOKEN);

        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void hashReturnsSameValueForSameToken() throws LilanyusziException {
        String firstHash = refreshTokenService.hash(RAW_TOKEN);
        String secondHash = refreshTokenService.hash(RAW_TOKEN);

        assertEquals(firstHash, secondHash);
    }

    @Test
    void hashReturnsDifferentValuesForDifferentTokens() throws LilanyusziException {
        String firstHash = refreshTokenService.hash(RAW_TOKEN);
        String secondHash = refreshTokenService.hash("other-token");

        assertNotEquals(firstHash, secondHash);
    }

    private RefreshToken refreshToken(User user, Instant expiresAt, Instant revokedAt) throws LilanyusziException {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(refreshTokenService.hash(RAW_TOKEN));
        refreshToken.setCreatedAt(Instant.now().minusSeconds(60));
        refreshToken.setExpiresAt(expiresAt);
        refreshToken.setRevokedAt(revokedAt);
        return refreshToken;
    }

    private User user() {
        User user = new User();
        user.setId(42L);
        user.setEmail("user@example.com");
        user.setRole(UserRole.USER);
        return user;
    }
}
