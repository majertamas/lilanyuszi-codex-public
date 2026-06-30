package com.lilanyuszi.app;

import com.lilanyuszi.app.api.MessageSeverity;
import com.lilanyuszi.app.user.User;
import com.lilanyuszi.app.user.UserRepository;
import com.lilanyuszi.app.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = {
        "app.test=user-controller-integration-test",
        "app.frontend.url.me=https://frontend.example/me",
        "app.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
        "spring.security.oauth2.client.registration.google.client-id=test-client-id",
        "spring.security.oauth2.client.registration.google.client-secret=test-client-secret"
})
@AutoConfigureMockMvc
class UserControllerIntegrationTest {

    private static final String ADMIN_EMAIL = "admin@example.com";
    private static final String USER_EMAIL = "user@example.com";
    public static final String API_USER_ME = "/api/user/me";
    public static final String ADMIN = "ADMIN";
    public static final String USER = "USER";
    public static final String API_USER_ADMIN_ME = "/api/user/admin/me";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        userRepository.save(persistedUser(ADMIN_EMAIL, UserRole.ADMIN));
        userRepository.save(persistedUser(USER_EMAIL, UserRole.USER));
    }

    @Test
    void meReturnsOkForAdminUser() throws Exception {
        mockMvc.perform(get(API_USER_ME)
                        .with(user(ADMIN_EMAIL).roles(ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(ADMIN_EMAIL))
                .andExpect(jsonPath("$.data.id").exists());
    }

    @Test
    void meReturnsOkForRegularUser() throws Exception {
        mockMvc.perform(get(API_USER_ME)
                        .with(user(USER_EMAIL).roles(USER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(USER_EMAIL))
                .andExpect(jsonPath("$.data.id").exists());
    }

    @Test
    void meReturnsUnauthorizedWithoutAuthentication() throws Exception {
        mockMvc.perform(get(API_USER_ME))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminMeReturnsOkForAdminUser() throws Exception {
        mockMvc.perform(get(API_USER_ADMIN_ME)
                        .with(user(ADMIN_EMAIL).roles(ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(ADMIN_EMAIL))
                .andExpect(jsonPath("$.data.id").exists());
    }

    @Test
    void adminMeReturnsAccessDeniedErrorForRegularUser() throws Exception {
        mockMvc.perform(get(API_USER_ADMIN_ME)
                        .with(user(USER_EMAIL).roles(USER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.messages[0].text").value("Access Denied"))
                .andExpect(jsonPath("$.messages[0].severity").value(MessageSeverity.ERROR.name()));
    }

    @Test
    void adminMeReturnsUnauthorizedWithoutAuthentication() throws Exception {
        mockMvc.perform(get(API_USER_ADMIN_ME))
                .andExpect(status().isUnauthorized());
    }

    private User persistedUser(String email, UserRole role) {
        Instant now = Instant.now();
        User user = new User();
        user.setEmail(email);
        user.setName(email);
        user.setRole(role);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        return user;
    }
}
