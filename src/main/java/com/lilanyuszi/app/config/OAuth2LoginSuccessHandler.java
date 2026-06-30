package com.lilanyuszi.app.config;

import com.lilanyuszi.app.api.LilanyusziException;
import com.lilanyuszi.app.auth.AuthCookieFactory;
import com.lilanyuszi.app.token.AccessTokenService;
import com.lilanyuszi.app.token.RefreshTokenService;
import com.lilanyuszi.app.user.User;
import com.lilanyuszi.app.user.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Value("${app.frontend.url.me}")
    private String frontendUrl;

    private final UserService userService;
    private final AccessTokenService accessTokenService;
    private final RefreshTokenService refreshTokenService;
    private final AuthCookieFactory authCookieFactory;

    @Override
    @Transactional
    public void onAuthenticationSuccess(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();

        assert oidcUser != null;
        User user = userService.findOrCreateOAuthUser(oidcUser);

        String accessToken = accessTokenService.generateAccessToken(user);
        String refreshToken;
        try {
            refreshToken = refreshTokenService.createRefreshToken(user);
        } catch (LilanyusziException e) {
            throw new ServletException("Failed to create refresh token", e);
        }

        var accessCookie = authCookieFactory.createAccessCookie(accessToken);
        var refreshCookie = authCookieFactory.createRefreshCookie(refreshToken);

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        getRedirectStrategy().sendRedirect(request, response, frontendUrl);
    }
}
