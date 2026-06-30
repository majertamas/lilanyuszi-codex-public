package com.lilanyuszi.app.user;

import com.lilanyuszi.app.api.LilanyusziException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.lilanyuszi.app.user.UserUtil.*;
import static com.lilanyuszi.app.util.Constant.USER_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    public static final String SAVED_USER = "SAVED USER: {}";
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    @Transactional
    public User findOrCreateOAuthUser(OidcUser oidcUser) {
        User user = userRepository.findByEmail(oidcUser.getEmail()).orElseGet(User::new);

        if (user.getId() == null) {
            return saveNewUser(user, oidcUser);
        }

        return user;
    }

    public UserResponse getUserData(Authentication authentication) {
        String email = authentication.getName();
        User byEmail = findByEmail(email);
        String picture = getPicture(byEmail);
        String name = getName(byEmail);
        String nickName = getNickname(byEmail);
        return new UserResponse(byEmail.getId(), email, picture, name, nickName);
    }

    @Transactional
    public User saveNewUser(User user, OidcUser oidcUser) {
        Instant now = Instant.now();

        user.setEmail(oidcUser.getEmail());
        user.setName(resolveName(oidcUser));
        user.setPicture(oidcUser.getPicture());
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        user.setRole(UserRole.USER);
        User savedUser = userRepository.save(user);
        log.info(SAVED_USER, savedUser);
        return savedUser;
    }

    private String resolveName(OidcUser oidcUser) {
        String fullName = oidcUser.getFullName();

        if (isUsableName(fullName)) {
            return fullName;
        }

        String name = oidcUser.getAttribute("name");
        if (isUsableName(name)) {
            return name;
        }

        String givenName = oidcUser.getGivenName();
        String familyName = oidcUser.getFamilyName();

        String combinedName = Stream.of(givenName, familyName)
                .filter(this::isUsableName)
                .collect(Collectors.joining(" "));

        if (isUsableName(combinedName)) {
            return combinedName;
        }

        return oidcUser.getEmail();
    }

    private boolean isUsableName(String value) {
        return value != null
                && !value.isBlank()
                && !value.matches("\\d+");
    }

    @Transactional
    public User addOrModify(String userAttribute, Map<String, String> body) throws LilanyusziException {

        User user = userRepository
                .findById(currentUserService.getAuthenticatedUserId())
                .orElseThrow(() -> new LilanyusziException(USER_NOT_FOUND));

        if (userAttribute.equalsIgnoreCase(UserAttribute.PICTURE.name().toLowerCase())) {
            user.setPicture(body.get(UserAttribute.PICTURE.name()));
        } else if (userAttribute.equalsIgnoreCase(UserAttribute.NICKNAME.name())) {
            user.setNickName(body.get(UserAttribute.NICKNAME.name().toLowerCase()));
        } else {
            throw new LilanyusziException("Invalid attribute");
        }

        user.setUpdatedAt(Instant.now());

        User saved = userRepository.save(user);

        log.info(SAVED_USER, saved);

        return saved;

    }
}
