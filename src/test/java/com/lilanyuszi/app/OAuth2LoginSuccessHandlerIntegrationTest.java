package com.lilanyuszi.app;

import com.lilanyuszi.app.config.OAuth2LoginSuccessHandler;
import com.lilanyuszi.app.token.RefreshTokenRepository;
import com.lilanyuszi.app.user.User;
import com.lilanyuszi.app.user.UserRepository;
import com.lilanyuszi.app.user.UserRole;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static com.lilanyuszi.app.util.Constant.ACCESS_TOKEN;
import static com.lilanyuszi.app.util.Constant.REFRESH_TOKEN;
import static org.junit.jupiter.api.Assertions.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = {
        "app.test=oauth-integration-test",
        "app.frontend.url.me=https://frontend.example/me",
        "app.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
        "spring.security.oauth2.client.registration.google.client-id=test-client-id",
        "spring.security.oauth2.client.registration.google.client-secret=test-client-secret"
})
class OAuth2LoginSuccessHandlerIntegrationTest {

    private static final String EMAIL = "new.user@example.com";
    public static final String USER_PNG = "https://example.com/new-user.png";
    public static final String NEW_USER = "New User";
    public static final String EXISTING_USER = "Existing User";
    public static final String EXISTING_USER_PNG = "https://example.com/existing-user.png";

    @Autowired
    private OAuth2LoginSuccessHandler successHandler;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createsNewUserAndAuthCookiesAfterGoogleLogin() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        Authentication authentication = new TestingAuthenticationToken(googleUser(), null);

        successHandler.onAuthenticationSuccess(request, response, authentication);

        User user = userRepository.findByEmail(EMAIL).orElseThrow();
        assertNotNull(user.getId());
        assertEquals(EMAIL, user.getEmail());
        assertEquals(NEW_USER, user.getName());
        assertEquals(USER_PNG, user.getPicture());
        assertEquals(UserRole.USER, user.getRole());
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());

        assertEquals(1, refreshTokenRepository.count());
        assertEquals(user.getId(), refreshTokenRepository.findAll().get(0).getUser().getId());

        List<String> setCookieHeaders = response.getHeaders(HttpHeaders.SET_COOKIE);
        assertTrue(setCookieHeaders.stream().anyMatch(cookie -> cookie.startsWith(ACCESS_TOKEN + "=")));
        assertTrue(setCookieHeaders.stream().anyMatch(cookie -> cookie.startsWith(REFRESH_TOKEN + "=")));
        assertEquals(HttpServletResponse.SC_FOUND, response.getStatus());
        assertEquals("https://frontend.example/me", response.getRedirectedUrl());
    }

    @Test
    void createsAuthCookiesForExistingUserAfterGoogleLogin() throws Exception {
        Instant now = Instant.now();
        User existingUser = new User();
        existingUser.setEmail(EMAIL);
        existingUser.setName(EXISTING_USER);
        existingUser.setPicture(EXISTING_USER_PNG);
        existingUser.setRole(UserRole.USER);
        existingUser.setCreatedAt(now.minusSeconds(60));
        existingUser.setUpdatedAt(now.minusSeconds(30));
        existingUser = userRepository.save(existingUser);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        Authentication authentication = new TestingAuthenticationToken(googleUser(), null);

        successHandler.onAuthenticationSuccess(request, response, authentication);

        User user = userRepository.findByEmail(EMAIL).orElseThrow();
        assertEquals(existingUser.getId(), user.getId());
        assertEquals(1, userRepository.count());
        assertEquals(EXISTING_USER, user.getName());
        assertEquals(EXISTING_USER_PNG, user.getPicture());

        assertEquals(1, refreshTokenRepository.count());
        assertEquals(existingUser.getId(), refreshTokenRepository.findAll().get(0).getUser().getId());

        List<String> setCookieHeaders = response.getHeaders(HttpHeaders.SET_COOKIE);
        assertTrue(setCookieHeaders.stream().anyMatch(cookie -> cookie.startsWith(ACCESS_TOKEN + "=")));
        assertTrue(setCookieHeaders.stream().anyMatch(cookie -> cookie.startsWith(REFRESH_TOKEN + "=")));
        assertEquals(HttpServletResponse.SC_FOUND, response.getStatus());
        assertEquals("https://frontend.example/me", response.getRedirectedUrl());
    }

    private OidcUser googleUser() {
        Instant now = Instant.now();
        Map<String, Object> claims = Map.of(
                "sub", "google-user-123",
                "email", EMAIL,
                "email_verified", true,
                "name", NEW_USER,
                "given_name", "New",
                "family_name", "User",
                "picture", USER_PNG
        );
        OidcIdToken idToken = new OidcIdToken("id-token", now, now.plusSeconds(300), claims);

        return new DefaultOidcUser(List.of(), idToken);
    }
}
