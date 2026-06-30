package com.lilanyuszi.app.shared_access_alias;

import java.time.Instant;

public record SharedAccessAliasResponse(
        Long id,
        Long sharedAccessId,
        Long userId,
        String alias,
        Instant createdAt,
        Instant updatedAt
) {
}
