package com.lilanyuszi.app.token;

import com.lilanyuszi.app.api.LilanyusziException;
import com.lilanyuszi.app.user.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

import static com.lilanyuszi.app.util.Constant.REFRESH_TOKEN_EXPIRATION_DAYS;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public String createRefreshToken(User user) throws LilanyusziException {
        byte[] randomBytes = new byte[64];
        secureRandom.nextBytes(randomBytes);

        String rawToken = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(hash(rawToken));
        refreshToken.setCreatedAt(Instant.now());
        refreshToken.setExpiresAt(Instant.now().plus(REFRESH_TOKEN_EXPIRATION_DAYS, ChronoUnit.DAYS));

        refreshTokenRepository.save(refreshToken);

        return rawToken;
    }

    @Transactional
    public RefreshRotationResult rotateAndGetUser(String rawToken) throws LilanyusziException {
        RefreshToken currentToken = refreshTokenRepository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (currentToken.getRevokedAt() != null) {
            throw new LilanyusziException("Refresh token revoked");
        }

        if (currentToken.getExpiresAt().isBefore(Instant.now())) {
            throw new LilanyusziException("Refresh token expired");
        }

        User user = currentToken.getUser();

        currentToken.setRevokedAt(Instant.now());
        refreshTokenRepository.save(currentToken);

        String newRefreshToken = createRefreshToken(user);

        return new RefreshRotationResult(user, newRefreshToken);
    }

    public void revoke(String rawToken) throws LilanyusziException {
        String tokenHash = hash(rawToken);

        refreshTokenRepository.findByTokenHash(tokenHash)
                .ifPresent(refreshToken -> {
                    refreshToken.setRevokedAt(Instant.now());
                    refreshTokenRepository.save(refreshToken);
                });
    }

    public String hash(String rawToken) throws LilanyusziException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new LilanyusziException("Could not hash refresh token");
        }
    }
}
