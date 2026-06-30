package com.lilanyuszi.app.auth;

import com.lilanyuszi.app.api.LilanyusziException;
import com.lilanyuszi.app.api.RestResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;

import static com.lilanyuszi.app.util.Constant.ACCESS_TOKEN;
import static com.lilanyuszi.app.util.Constant.REFRESH_TOKEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private static final String OLD_REFRESH_TOKEN = "old-refresh-token";
    private static final String NEW_ACCESS_TOKEN = "new-access-token";
    private static final String NEW_REFRESH_TOKEN = "new-refresh-token";

    @Mock
    private AuthSessionService authSessionService;

    @Mock
    private AuthCookieFactory authCookieFactory;

    @InjectMocks
    private AuthController authController;

    @Test
    void refreshDelegatesToServiceAddsCookiesAndReturnsNoContent() throws LilanyusziException {
        MockHttpServletResponse response = new MockHttpServletResponse();
        AuthSession session = new AuthSession(NEW_ACCESS_TOKEN, NEW_REFRESH_TOKEN);
        ResponseCookie accessCookie = ResponseCookie.from(ACCESS_TOKEN, NEW_ACCESS_TOKEN).build();
        ResponseCookie refreshCookie = ResponseCookie.from(REFRESH_TOKEN, NEW_REFRESH_TOKEN).build();
        when(authSessionService.refreshSession(OLD_REFRESH_TOKEN)).thenReturn(session);
        when(authCookieFactory.createAccessCookie(NEW_ACCESS_TOKEN)).thenReturn(accessCookie);
        when(authCookieFactory.createRefreshCookie(NEW_REFRESH_TOKEN)).thenReturn(refreshCookie);

        ResponseEntity<RestResponse<Void>> result = authController.refresh(OLD_REFRESH_TOKEN, response);

        assertEquals(204, result.getStatusCode().value());
        assertNull(result.getBody());
        assertEquals(accessCookie.toString(), response.getHeaders(HttpHeaders.SET_COOKIE).get(0));
        assertEquals(refreshCookie.toString(), response.getHeaders(HttpHeaders.SET_COOKIE).get(1));
        verify(authSessionService).refreshSession(OLD_REFRESH_TOKEN);
    }

    @Test
    void logoutDelegatesToServiceAddsDeleteCookiesAndReturnsNoContent() throws LilanyusziException {
        MockHttpServletResponse response = new MockHttpServletResponse();
        ResponseCookie deleteAccessCookie = ResponseCookie.from(ACCESS_TOKEN, "").maxAge(0).build();
        ResponseCookie deleteRefreshCookie = ResponseCookie.from(REFRESH_TOKEN, "").maxAge(0).build();
        when(authCookieFactory.deleteAccessCookie()).thenReturn(deleteAccessCookie);
        when(authCookieFactory.deleteRefreshCookie()).thenReturn(deleteRefreshCookie);

        ResponseEntity<RestResponse<Void>> result = authController.logout(OLD_REFRESH_TOKEN, response);

        assertEquals(204, result.getStatusCode().value());
        assertNull(result.getBody());
        assertDeleteCookieHeader(response.getHeaders(HttpHeaders.SET_COOKIE).get(0), ACCESS_TOKEN);
        assertDeleteCookieHeader(response.getHeaders(HttpHeaders.SET_COOKIE).get(1), REFRESH_TOKEN);
        verify(authSessionService).logout(OLD_REFRESH_TOKEN);
    }

    @Test
    void logoutAllowsMissingRefreshToken() throws LilanyusziException {
        MockHttpServletResponse response = new MockHttpServletResponse();
        ResponseCookie deleteAccessCookie = ResponseCookie.from(ACCESS_TOKEN, "").maxAge(0).build();
        ResponseCookie deleteRefreshCookie = ResponseCookie.from(REFRESH_TOKEN, "").maxAge(0).build();
        when(authCookieFactory.deleteAccessCookie()).thenReturn(deleteAccessCookie);
        when(authCookieFactory.deleteRefreshCookie()).thenReturn(deleteRefreshCookie);

        ResponseEntity<RestResponse<Void>> result = authController.logout(null, response);

        assertEquals(204, result.getStatusCode().value());
        assertNull(result.getBody());
        assertDeleteCookieHeader(response.getHeaders(HttpHeaders.SET_COOKIE).get(0), ACCESS_TOKEN);
        assertDeleteCookieHeader(response.getHeaders(HttpHeaders.SET_COOKIE).get(1), REFRESH_TOKEN);
        verify(authSessionService).logout(null);
    }
    private void assertDeleteCookieHeader(String header, String cookieName) {
        assertTrue(header.startsWith(cookieName + "="));
        assertTrue(header.contains("Max-Age=0"));
    }

}
