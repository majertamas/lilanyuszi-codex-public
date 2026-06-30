package com.lilanyuszi.app.token;

import com.lilanyuszi.app.user.User;

public record RefreshRotationResult(
        User user,
        String refreshToken
) {
}
