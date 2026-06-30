package com.lilanyuszi.app.user;

import com.lilanyuszi.app.api.LilanyusziException;
import com.lilanyuszi.app.api.RestResponse;
import com.lilanyuszi.app.api.RestResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<RestResponse<UserResponse>> me(Authentication authentication) {
        return ResponseEntity.ok(RestResponseUtil.createRestResponse(userService.getUserData(authentication)));
    }

    @PutMapping("/me/{userAttribute}")
    public ResponseEntity<User> addOrModifyPicture(@PathVariable String userAttribute,
                                                   @RequestBody Map<String, String> body) throws LilanyusziException {
        User updatedUser = userService.addOrModify(userAttribute, body);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/admin/me")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RestResponse<UserResponse>> adminMe(Authentication authentication) {
        return ResponseEntity.ok(RestResponseUtil.createRestResponse(userService.getUserData(authentication)));
    }
}
