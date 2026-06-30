package com.lilanyuszi.app.shared_access;

import com.lilanyuszi.app.api.LilanyusziException;
import com.lilanyuszi.app.shared_access_alias.SharedAccessAliasService;
import com.lilanyuszi.app.shared_access_member.SharedAccessMemberService;
import com.lilanyuszi.app.shopping_list.ShoppingList;
import com.lilanyuszi.app.shopping_list.ShoppingListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SharedAccessDeletionService {

    private final SharedAccessAliasService sharedAccessAliasService;
    private final SharedAccessMemberService sharedAccessMemberService;
    private final ShoppingListRepository shoppingListRepository;
    private final SharedAccessRepository sharedAccessRepository;

    @Transactional
    public void deleteAliasAndMembership(Long sharedAccessId, Long userId) {
        sharedAccessAliasService.deleteBySharedAccessIdAndUserId(sharedAccessId, userId);
        sharedAccessMemberService.deleteBySharedAccessIdAndUserId(sharedAccessId, userId);
    }

    @Transactional
    public void deleteList(Long sharedAccessId, SharedAccessType type) throws LilanyusziException {
        switch (type) {
            case SHOPPING -> deleteShoppingList(sharedAccessId);
            case RECIPE -> throw new LilanyusziException("Recipe list deletion is not implemented yet");
            default -> throw new LilanyusziException("Unsupported shared access type: " + type);
        }
    }

    private void deleteShoppingList(Long sharedAccessId) throws LilanyusziException {
        ShoppingList shoppingList = shoppingListRepository.findBySharedAccessId(sharedAccessId)
                .orElseThrow(() -> new LilanyusziException("Shopping list can not be found"));

        sharedAccessAliasService.deleteBySharedAccessId(sharedAccessId);
        sharedAccessMemberService.deleteBySharedAccessId(sharedAccessId);
        shoppingListRepository.delete(shoppingList);
        sharedAccessRepository.deleteById(sharedAccessId);
    }
}
