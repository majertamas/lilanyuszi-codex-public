package com.lilanyuszi.app.config;

import com.lilanyuszi.app.token.AccessTokenService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static com.lilanyuszi.app.util.Constant.ACCESS_TOKEN;
import static com.lilanyuszi.app.util.Constant.API_AUTH_LOGOUT_PATH;
import static com.lilanyuszi.app.util.Constant.API_AUTH_REFRESH_PATH;
import static com.lilanyuszi.app.util.Constant.AUTHORIZATION;
import static com.lilanyuszi.app.util.Constant.BEARER_PLUS_EMPTY;
import static com.lilanyuszi.app.util.Constant.JWT_CLAIM_EMAIL;
import static com.lilanyuszi.app.util.Constant.JWT_CLAIM_ROLE;
import static com.lilanyuszi.app.util.Constant.OAUTH2_PATH_PREFIX;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    private static final String TOKEN = "access-token";
    private static final String COOKIE_TOKEN = "cookie-access-token";
    private static final String USER_ID = "42";
    private static final String EMAIL = "user@example.com";
    private static final String ROLE = "ADMIN";

    @Mock
    private AccessTokenService accessTokenService;

    @Mock
    private FilterChain filterChain;

    @Mock
    private Claims claims;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldNotFilterRefreshEndpoint() {
        MockHttpServletRequest request = request(API_AUTH_REFRESH_PATH);

        assertTrue(jwtAuthenticationFilter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilterLogoutEndpoint() {
        MockHttpServletRequest request = request(API_AUTH_LOGOUT_PATH);

        assertTrue(jwtAuthenticationFilter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilterOauth2Endpoint() {
        MockHttpServletRequest request = request(OAUTH2_PATH_PREFIX + "authorization/google");

        assertTrue(jwtAuthenticationFilter.shouldNotFilter(request));
    }

    @Test
    void shouldFilterOtherEndpoints() {
        MockHttpServletRequest request = request("/api/user/me");

        assertFalse(jwtAuthenticationFilter.shouldNotFilter(request));
    }

    @Test
    void continuesFilterChainWhenTokenIsMissing() throws Exception {
        MockHttpServletRequest request = request("/api/user/me");
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(accessTokenService, never()).parseAccessToken(TOKEN);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void continuesFilterChainWhenAuthorizationHeaderIsNotBearerAndCookieIsMissing() throws Exception {
        MockHttpServletRequest request = request("/api/user/me");
        request.addHeader(AUTHORIZATION, "Basic abc");
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(accessTokenService, never()).parseAccessToken(TOKEN);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void continuesFilterChainWhenCookiesExistButAccessTokenCookieIsMissing() throws Exception {
        MockHttpServletRequest request = request("/api/user/me");
        request.setCookies(new Cookie("other_cookie", "other-token"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(accessTokenService, never()).parseAccessToken(TOKEN);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void authenticatesUserFromBearerToken() throws Exception {
        MockHttpServletRequest request = request("/api/user/me");
        request.addHeader(AUTHORIZATION, BEARER_PLUS_EMPTY + TOKEN);
        MockHttpServletResponse response = new MockHttpServletResponse();
        mockClaims();
        when(accessTokenService.parseAccessToken(TOKEN)).thenReturn(claims);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals(EMAIL, authentication.getPrincipal());
        assertEquals(USER_ID, authentication.getDetails());
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority())));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void authenticatesUserFromAccessTokenCookie() throws Exception {
        MockHttpServletRequest request = request("/api/user/me");
        request.setCookies(new Cookie(ACCESS_TOKEN, COOKIE_TOKEN));
        MockHttpServletResponse response = new MockHttpServletResponse();
        mockClaims();
        when(accessTokenService.parseAccessToken(COOKIE_TOKEN)).thenReturn(claims);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals(EMAIL, authentication.getPrincipal());
        assertEquals(USER_ID, authentication.getDetails());
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority())));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void bearerTokenTakesPrecedenceOverAccessTokenCookie() throws Exception {
        MockHttpServletRequest request = request("/api/user/me");
        request.addHeader(AUTHORIZATION, BEARER_PLUS_EMPTY + TOKEN);
        request.setCookies(new Cookie(ACCESS_TOKEN, COOKIE_TOKEN));
        MockHttpServletResponse response = new MockHttpServletResponse();
        mockClaims();
        when(accessTokenService.parseAccessToken(TOKEN)).thenReturn(claims);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(accessTokenService).parseAccessToken(TOKEN);
        verify(accessTokenService, never()).parseAccessToken(COOKIE_TOKEN);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void clearsSecurityContextAndWritesUnauthorizedResponseWhenTokenIsInvalid() throws Exception {
        MockHttpServletRequest request = request("/api/user/me");
        request.addHeader(AUTHORIZATION, BEARER_PLUS_EMPTY + TOKEN);
        MockHttpServletResponse response = new MockHttpServletResponse();
        SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.TestingAuthenticationToken("previous", null)
        );
        when(accessTokenService.parseAccessToken(TOKEN)).thenThrow(new IllegalArgumentException("bad token"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(401, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());
        assertTrue(response.getContentAsString().contains("Invalid or expired access token"));
        verify(filterChain).doFilter(request, response);
    }

    private MockHttpServletRequest request(String path) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(path);
        return request;
    }

    private void mockClaims() {
        when(claims.getSubject()).thenReturn(USER_ID);
        when(claims.get(JWT_CLAIM_EMAIL, String.class)).thenReturn(EMAIL);
        when(claims.get(JWT_CLAIM_ROLE, String.class)).thenReturn(ROLE);
    }
}
