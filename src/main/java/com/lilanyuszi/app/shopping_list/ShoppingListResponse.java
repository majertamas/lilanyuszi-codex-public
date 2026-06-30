package com.lilanyuszi.app.shopping_list;

import java.time.Instant;

public record ShoppingListResponse(
        Long id,
        Long sharedAccessId,
        String name,
        String alias,
        Instant createdAt,
        Instant updatedAt
) {
}
