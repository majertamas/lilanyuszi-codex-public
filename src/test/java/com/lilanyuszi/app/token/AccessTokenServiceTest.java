package com.lilanyuszi.app.token;

import com.lilanyuszi.app.user.User;
import com.lilanyuszi.app.user.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static com.lilanyuszi.app.util.Constant.JWT_CLAIM_EMAIL;
import static com.lilanyuszi.app.util.Constant.JWT_CLAIM_ROLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccessTokenServiceTest {

    private static final String SECRET = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

    private final AccessTokenService accessTokenService = new AccessTokenService(SECRET);

    @Test
    void generateAccessTokenCreatesTokenWithExpectedClaims() {
        User user = user();

        String token = accessTokenService.generateAccessToken(user);
        Claims claims = accessTokenService.parseAccessToken(token);

        assertEquals("42", claims.getSubject());
        assertEquals("user@example.com", claims.get(JWT_CLAIM_EMAIL, String.class));
        assertEquals(UserRole.ADMIN.name(), claims.get(JWT_CLAIM_ROLE, String.class));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
        assertTrue(claims.getExpiration().after(claims.getIssuedAt()));
    }

    @Test
    void parseAccessTokenThrowsWhenTokenIsInvalid() {
        assertThrows(JwtException.class, () -> accessTokenService.parseAccessToken("invalid-token"));
    }

    @Test
    void parseAccessTokenThrowsWhenTokenWasSignedWithDifferentSecret() {
        AccessTokenService otherService = new AccessTokenService("YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY=");
        String token = otherService.generateAccessToken(user());

        assertThrows(JwtException.class, () -> accessTokenService.parseAccessToken(token));
    }

    private User user() {
        User user = new User();
        user.setId(42L);
        user.setEmail("user@example.com");
        user.setRole(UserRole.ADMIN);
        return user;
    }
}
