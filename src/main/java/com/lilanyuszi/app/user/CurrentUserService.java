package com.lilanyuszi.app.user;

import com.lilanyuszi.app.api.LilanyusziException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import static com.lilanyuszi.app.util.Constant.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    public User getAuthenticatedUser() throws LilanyusziException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new LilanyusziException("User is not authenticated");
        }

        String email = (String) authentication.getPrincipal();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new LilanyusziException(USER_NOT_FOUND));
    }

    public Long getAuthenticatedUserId() throws LilanyusziException {
        return getAuthenticatedUser().getId();
    }
}