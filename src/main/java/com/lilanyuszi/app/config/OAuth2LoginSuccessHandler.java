package com.lilanyuszi.app.config;

import com.lilanyuszi.app.api.LilanyusziException;
import com.lilanyuszi.app.auth.AuthCookieFactory;
import com.lilanyuszi.app.auth.AuthSession;
import com.lilanyuszi.app.auth.AuthSessionService;
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
    private final AuthSessionService authSessionService;
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
        AuthSession session;
        try {
            session = authSessionService.createOAuthSession(oidcUser);
        } catch (LilanyusziException e) {
            throw new ServletException(e);
        }

        var accessCookie = authCookieFactory.createAccessCookie(session.accessToken());
        var refreshCookie = authCookieFactory.createRefreshCookie(session.refreshToken());

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        getRedirectStrategy().sendRedirect(request, response, frontendUrl);
    }
}
