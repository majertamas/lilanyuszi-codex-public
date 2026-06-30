package com.lilanyuszi.app.shared_access_member;

import com.lilanyuszi.app.shared_access.SharedAccessRole;

public record SharedAccessMemberResponse(
        String name,
        SharedAccessRole role,
        Long userId
) {
}
