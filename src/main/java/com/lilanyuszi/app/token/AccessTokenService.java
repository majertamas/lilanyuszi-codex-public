package com.lilanyuszi.app.token;

import com.lilanyuszi.app.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static com.lilanyuszi.app.util.Constant.ACCESS_TOKEN_EXPIRATION_MINUTES;
import static com.lilanyuszi.app.util.Constant.JWT_CLAIM_EMAIL;
import static com.lilanyuszi.app.util.Constant.JWT_CLAIM_ROLE;

@Service
public class AccessTokenService {

    private final SecretKey key;

    public AccessTokenService(@Value("${app.jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(secret)
        );
    }

    public Claims parseAccessToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String generateAccessToken(User user) {
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim(JWT_CLAIM_EMAIL, user.getEmail())
                .claim(JWT_CLAIM_ROLE, user.getRole().name())
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plus(ACCESS_TOKEN_EXPIRATION_MINUTES, ChronoUnit.MINUTES)))
                .signWith(key)
                .compact();
    }

}
