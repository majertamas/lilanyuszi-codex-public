package com.lilanyuszi.app.auth;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthCookieFactoryTest {

    private final AuthCookieFactory authCookieFactory = new AuthCookieFactory();

    @Test
    void createsAccessCookie() {
        ResponseCookie cookie = authCookieFactory.createAccessCookie("access-token");

        assertEquals("access_token", cookie.getName());
        assertEquals("access-token", cookie.getValue());
        assertCookieDefaults(cookie);
        assertEquals(900, cookie.getMaxAge().toSeconds());
    }

    @Test
    void createsRefreshCookie() {
        ResponseCookie cookie = authCookieFactory.createRefreshCookie("refresh-token");

        assertEquals("refresh_token", cookie.getName());
        assertEquals("refresh-token", cookie.getValue());
        assertCookieDefaults(cookie);
        assertEquals(2_592_000, cookie.getMaxAge().toSeconds());
    }

    @Test
    void createsDeleteCookies() {
        ResponseCookie accessCookie = authCookieFactory.deleteAccessCookie();
        ResponseCookie refreshCookie = authCookieFactory.deleteRefreshCookie();

        assertEquals("access_token", accessCookie.getName());
        assertEquals("", accessCookie.getValue());
        assertEquals(0, accessCookie.getMaxAge().toSeconds());
        assertCookieDefaults(accessCookie);

        assertEquals("refresh_token", refreshCookie.getName());
        assertEquals("", refreshCookie.getValue());
        assertEquals(0, refreshCookie.getMaxAge().toSeconds());
        assertCookieDefaults(refreshCookie);
    }

    private void assertCookieDefaults(ResponseCookie cookie) {
        assertTrue(cookie.isHttpOnly());
        assertFalse(cookie.isSecure());
        assertEquals("Lax", cookie.getSameSite());
        assertEquals("/", cookie.getPath());
    }
}
