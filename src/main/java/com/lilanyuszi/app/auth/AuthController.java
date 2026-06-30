package com.lilanyuszi.app.auth;

import com.lilanyuszi.app.api.LilanyusziException;
import com.lilanyuszi.app.api.RestResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.lilanyuszi.app.util.Constant.API_AUTH_PATH;
import static com.lilanyuszi.app.util.Constant.AUTH_LOGOUT_PATH;
import static com.lilanyuszi.app.util.Constant.AUTH_REFRESH_PATH;
import static com.lilanyuszi.app.util.Constant.REFRESH_TOKEN;

@RestController
@RequestMapping(API_AUTH_PATH)
@RequiredArgsConstructor
public class AuthController {

    private final AuthSessionService authSessionService;
    private final AuthCookieFactory authCookieFactory;

    @PostMapping(AUTH_REFRESH_PATH)
    public ResponseEntity<RestResponse<Void>> refresh(
            @CookieValue(name = REFRESH_TOKEN) String refreshToken,
            HttpServletResponse response
    ) throws LilanyusziException {
        AuthSession session = authSessionService.refreshSession(refreshToken);

        var accessCookie = authCookieFactory.createAccessCookie(session.accessToken());
        var refreshCookie = authCookieFactory.createRefreshCookie(session.refreshToken());

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return ResponseEntity.noContent().build();
    }

    @PostMapping(AUTH_LOGOUT_PATH)
    public ResponseEntity<RestResponse<Void>> logout(
            @CookieValue(name = REFRESH_TOKEN, required = false) String refreshToken,
            HttpServletResponse response
    ) throws LilanyusziException {
        authSessionService.logout(refreshToken);

        var deleteAccessCookie = authCookieFactory.deleteAccessCookie();
        var deleteRefreshCookie = authCookieFactory.deleteRefreshCookie();

        response.addHeader(HttpHeaders.SET_COOKIE, deleteAccessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, deleteRefreshCookie.toString());

        return ResponseEntity.noContent().build();
    }
}
