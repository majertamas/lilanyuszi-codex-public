package com.lilanyuszi.app.user;

import com.lilanyuszi.app.api.LilanyusziException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static com.lilanyuszi.app.util.Constant.USER_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrentUserServiceTest {

    private static final String EMAIL = "user@example.com";
    private static final String USER_IS_NOT_AUTHENTICATED = "User is not authenticated";

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CurrentUserService currentUserService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getAuthenticatedUserReturnsUserFromSecurityContext() throws LilanyusziException {
        User user = user(1L);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(EMAIL, null, List.of()));
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        User result = currentUserService.getAuthenticatedUser();

        assertSame(user, result);
    }

    @Test
    void getAuthenticatedUserThrowsWhenAuthenticationIsMissing() {
        LilanyusziException exception = assertThrows(
                LilanyusziException.class,
                () -> currentUserService.getAuthenticatedUser()
        );

        assertEquals(USER_IS_NOT_AUTHENTICATED, exception.getExMessage().getText());
    }

    @Test
    void getAuthenticatedUserThrowsWhenAuthenticationIsNotAuthenticated() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(EMAIL, null);
        authentication.setAuthenticated(false);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        LilanyusziException exception = assertThrows(
                LilanyusziException.class,
                () -> currentUserService.getAuthenticatedUser()
        );

        assertEquals(USER_IS_NOT_AUTHENTICATED, exception.getExMessage().getText());
    }

    @Test
    void getAuthenticatedUserThrowsWhenUserIsMissing() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(EMAIL, null, List.of()));
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        LilanyusziException exception = assertThrows(
                LilanyusziException.class,
                () -> currentUserService.getAuthenticatedUser()
        );

        assertEquals(USER_NOT_FOUND, exception.getExMessage().getText());
    }

    @Test
    void getAuthenticatedUserIdReturnsAuthenticatedUserId() throws LilanyusziException {
        User user = user(42L);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(EMAIL, null, List.of()));
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        Long userId = currentUserService.getAuthenticatedUserId();

        assertEquals(42L, userId);
    }

    private User user(Long id) {
        User user = new User();
        user.setId(id);
        user.setEmail(EMAIL);
        user.setRole(UserRole.USER);
        return user;
    }
}
