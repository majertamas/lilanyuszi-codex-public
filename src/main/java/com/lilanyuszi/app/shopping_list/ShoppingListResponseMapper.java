package com.lilanyuszi.app.shopping_list;

import com.lilanyuszi.app.shared_access.SharedAccess;
import com.lilanyuszi.app.shared_access_alias.SharedAccessAliasService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShoppingListResponseMapper {

    private final SharedAccessAliasService sharedAccessAliasService;

    public ShoppingListResponse toResponse(ShoppingList shoppingList, Long userId) {
        SharedAccess sharedAccess = shoppingList.getSharedAccess();
        String sharedAccessName = sharedAccess.getName();
        String alias = sharedAccessAliasService
                .findAliasBySharedAccessIdAndUserId(sharedAccess.getId(), userId)
                .orElse(sharedAccessName);
        return new ShoppingListResponse(shoppingList.getId(), sharedAccess.getId(), sharedAccessName, alias, sharedAccess.getCreatedAt(), sharedAccess.getUpdatedAt());
    }
}
