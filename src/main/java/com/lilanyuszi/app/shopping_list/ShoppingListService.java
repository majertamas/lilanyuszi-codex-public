package com.lilanyuszi.app.shopping_list;

import com.lilanyuszi.app.api.LilanyusziException;
import com.lilanyuszi.app.shared_access.SharedAccess;
import com.lilanyuszi.app.shared_access.SharedAccessDeletionService;
import com.lilanyuszi.app.shared_access.SharedAccessService;
import com.lilanyuszi.app.shared_access.SharedAccessType;
import com.lilanyuszi.app.shared_access_member.SharedAccessMember;
import com.lilanyuszi.app.shared_access_member.SharedAccessMemberService;
import com.lilanyuszi.app.user.CurrentUserService;
import com.lilanyuszi.app.user.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.lilanyuszi.app.util.Constant.SHOPPING_LIST_ACCESS_DENIED;
import static com.lilanyuszi.app.util.Constant.SHOPPING_LIST_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShoppingListService {

    private final ShoppingListRepository shoppingListRepository;
    private final SharedAccessService sharedAccessService;
    private final SharedAccessMemberService sharedAccessMemberService;
    private final CurrentUserService currentUserService;
    private final ShoppingListResponseMapper shoppingListResponseMapper;
    private final SharedAccessDeletionService sharedAccessDeletionService;

    @Transactional
    public ShoppingListResponse create(ShoppingListCreateRequest request) throws LilanyusziException {
        User user = currentUserService.getAuthenticatedUser();
        SharedAccess sharedAccess = sharedAccessService.create(user, SharedAccessType.SHOPPING, request.name());

        ShoppingList shoppingList = shoppingListRepository.save(ShoppingList.builder()
                .sharedAccess(sharedAccess)
                .build());

        sharedAccessMemberService.save(SharedAccessMember.builder()
                .sharedAccess(sharedAccess)
                .user(user)
                .build());

        log.info("CREATED SHOPPING LIST ID: {}, OWNER USER ID: {}", shoppingList.getId(), user.getId());
        return shoppingListResponseMapper.toResponse(shoppingList, user.getId());
    }

    public ShoppingListResponse findById(Long id) throws LilanyusziException {
        Long userId = currentUserService.getAuthenticatedUserId();
        ShoppingList shoppingList = findAccessibleShoppingList(id, userId);
        return shoppingListResponseMapper.toResponse(shoppingList, userId);
    }

    public List<ShoppingListResponse> findAllForAuthenticatedUser() throws LilanyusziException {
        Long userId = currentUserService.getAuthenticatedUserId();
        List<Long> sharedAccessIds = sharedAccessMemberService.findByUserId(userId).stream()
                .map(member -> member.getSharedAccess().getId())
                .toList();

        return shoppingListRepository.findBySharedAccessIdIn(sharedAccessIds).stream()
                .map(shoppingList -> shoppingListResponseMapper.toResponse(shoppingList, userId))
                .toList();
    }

    @Transactional
    public void deleteById(Long id) throws LilanyusziException {
        User user = currentUserService.getAuthenticatedUser();
        ShoppingList shoppingList = shoppingListRepository.findById(id)
                .orElseThrow(() -> new LilanyusziException(SHOPPING_LIST_NOT_FOUND));

        if (!isOwner(shoppingList.getSharedAccess(), user.getId())) {
            throw new LilanyusziException(SHOPPING_LIST_ACCESS_DENIED);
        }

        sharedAccessDeletionService.deleteList(shoppingList.getSharedAccess().getId(), SharedAccessType.SHOPPING);
    }

    @Transactional
    public void leave(Long id) throws LilanyusziException {
        User user = currentUserService.getAuthenticatedUser();
        ShoppingList shoppingList = shoppingListRepository.findById(id)
                .orElseThrow(() -> new LilanyusziException(SHOPPING_LIST_NOT_FOUND));
        Long sharedAccessId = shoppingList.getSharedAccess().getId();

        if (isOwner(shoppingList.getSharedAccess(), user.getId())) {
            sharedAccessDeletionService.deleteList(shoppingList.getSharedAccess().getId(), SharedAccessType.SHOPPING);
            return;
        }

        if (!sharedAccessMemberService.existsBySharedAccessIdAndUserId(sharedAccessId, user.getId())) {
            throw new LilanyusziException(SHOPPING_LIST_ACCESS_DENIED);
        }

        sharedAccessDeletionService.deleteAliasAndMembership(sharedAccessId, user.getId());
        log.info("USER ID: {} LEFT SHOPPING LIST ID: {}", user.getId(), id);
    }

    private ShoppingList findAccessibleShoppingList(Long id, Long userId) throws LilanyusziException {
        ShoppingList shoppingList = shoppingListRepository.findById(id)
                .orElseThrow(() -> new LilanyusziException(SHOPPING_LIST_NOT_FOUND));

        boolean member = sharedAccessMemberService.existsBySharedAccessIdAndUserId(
                shoppingList.getSharedAccess().getId(),
                userId
        );
        if (!member) {
            throw new LilanyusziException(SHOPPING_LIST_ACCESS_DENIED);
        }

        return shoppingList;
    }

    private boolean isOwner(SharedAccess sharedAccess, Long userId) {
        return sharedAccess.getOwnerUser().getId().equals(userId);
    }
}
