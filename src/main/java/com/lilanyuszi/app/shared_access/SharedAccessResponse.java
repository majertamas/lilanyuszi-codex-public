package com.lilanyuszi.app.shared_access;

import com.lilanyuszi.app.shared_access_member.SharedAccessMemberResponse;

import java.time.Instant;
import java.util.List;

public record SharedAccessResponse(
        Long id,
        String name,
        String alias,
        Instant createdAt,
        Instant updatedAt,
        SharedAccessType type,
        SharedAccessRole role,
        List<SharedAccessMemberResponse> members,
        boolean isOwner
) {
}