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
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.List;
import java.util.Optional;

import static com.lilanyuszi.app.util.Constant.USER_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final String EMAIL = "user@example.com";
    private static final String PICTURE = "https://example.com/user.png";
    private static final String FULL_NAME = "Full Name";
    private static final String USER_IS_NOT_AUTHENTICATED = "User is not authenticated";
    private static final String NICKNAME = "nickname";

    @Mock
    private UserRepository userRepository;

    @Mock
    private OidcUser oidcUser;

    @InjectMocks
    private UserService userService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void findByEmailReturnsUserWhenPresent() {
        User user = user(1L);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        User result = userService.findByEmail(EMAIL);

        assertSame(user, result);
    }

    @Test
    void findByEmailReturnsNullWhenMissing() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        User result = userService.findByEmail(EMAIL);

        assertNull(result);
    }

    @Test
    void findOrCreateOAuthUserReturnsExistingUser() {
        User existingUser = user(1L);
        when(oidcUser.getEmail()).thenReturn(EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existingUser));

        User result = userService.findOrCreateOAuthUser(oidcUser);

        assertSame(existingUser, result);
        verify(userRepository, never()).save(any());
    }

    @Test
    void findOrCreateOAuthUserCreatesUserWhenMissing() {
        mockOidcUser(FULL_NAME, null, null, null);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.findOrCreateOAuthUser(oidcUser);

        assertEquals(EMAIL, result.getEmail());
        assertEquals(FULL_NAME, result.getName());
        assertEquals(PICTURE, result.getPicture());
        assertEquals(UserRole.USER, result.getRole());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
        verify(userRepository).save(result);
    }

    @Test
    void getUserDataReturnsMappedUserResponse() {
        User user = user(1L);
        user.setPicture(PICTURE);
        user.setName(FULL_NAME);
        user.setNickName(NICKNAME);
        Authentication authentication = new UsernamePasswordAuthenticationToken(EMAIL, null);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        UserResponse response = userService.getUserData(authentication);

        assertEquals(1L, response.id());
        assertEquals(EMAIL, response.email());
        assertEquals(PICTURE, response.picture());
        assertEquals(FULL_NAME, response.name());
        assertEquals(NICKNAME, response.nickName());
    }

    @Test
    void saveNewUserUsesFullNameWhenUsable() {
        mockOidcUser(FULL_NAME, "Ignored Name", "Ignored", "Name");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.saveNewUser(new User(), oidcUser);

        assertEquals(FULL_NAME, result.getName());
    }

    @Test
    void saveNewUserUsesNameAttributeWhenFullNameIsNotUsable() {
        mockOidcUser("   ", "Attribute Name", "Ignored", "Name");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.saveNewUser(new User(), oidcUser);

        assertEquals("Attribute Name", result.getName());
    }

    @Test
    void saveNewUserUsesGivenAndFamilyNameWhenOtherNamesAreNotUsable() {
        mockOidcUser("123", "   ", "Given", "Family");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.saveNewUser(new User(), oidcUser);

        assertEquals("Given Family", result.getName());
    }

    @Test
    void saveNewUserFallsBackToEmailWhenNoUsableNameExists() {
        mockOidcUser(null, "123", "   ", null);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.saveNewUser(new User(), oidcUser);

        assertEquals(EMAIL, result.getName());
    }

    @Test
    void getAuthenticatedUserReturnsUserFromSecurityContext() throws LilanyusziException {
        User user = user(1L);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(EMAIL, null, List.of()));
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        User result = userService.getAuthenticatedUser();

        assertSame(user, result);
    }

    @Test
    void getAuthenticatedUserThrowsWhenAuthenticationIsMissing() {
        LilanyusziException exception = assertThrows(
                LilanyusziException.class,
                () -> userService.getAuthenticatedUser()
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
                () -> userService.getAuthenticatedUser()
        );

        assertEquals(USER_IS_NOT_AUTHENTICATED, exception.getExMessage().getText());
    }

    @Test
    void getAuthenticatedUserThrowsWhenUserIsMissing() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(EMAIL, null, List.of()));
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        LilanyusziException exception = assertThrows(
                LilanyusziException.class,
                () -> userService.getAuthenticatedUser()
        );

        assertEquals(USER_NOT_FOUND, exception.getExMessage().getText());
    }

    @Test
    void getAuthenticatedUserIdReturnsAuthenticatedUserId() throws LilanyusziException {
        User user = user(42L);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(EMAIL, null, List.of()));
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        Long userId = userService.getAuthenticatedUserId();

        assertEquals(42L, userId);
    }

    private void mockOidcUser(
            String fullName,
            String name,
            String givenName,
            String familyName
    ) {
        when(oidcUser.getEmail()).thenReturn(EMAIL);
        when(oidcUser.getFullName()).thenReturn(fullName);
        lenient().when(oidcUser.getAttribute("name")).thenReturn(name);
        lenient().when(oidcUser.getGivenName()).thenReturn(givenName);
        lenient().when(oidcUser.getFamilyName()).thenReturn(familyName);
        when(oidcUser.getPicture()).thenReturn(PICTURE);
    }

    private User user(Long id) {
        User user = new User();
        user.setId(id);
        user.setEmail(EMAIL);
        user.setRole(UserRole.USER);
        return user;
    }
}
