package com.lilanyuszi.app.shared_access_alias;

import com.lilanyuszi.app.api.LilanyusziException;
import com.lilanyuszi.app.shared_access.SharedAccess;
import com.lilanyuszi.app.shared_access.SharedAccessService;
import com.lilanyuszi.app.shared_access_member.SharedAccessMemberService;
import com.lilanyuszi.app.user.CurrentUserService;
import com.lilanyuszi.app.user.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.lilanyuszi.app.util.Constant.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SharedAccessAliasService {

    private final SharedAccessAliasRepository sharedAccessAliasRepository;
    private final SharedAccessService sharedAccessService;
    private final SharedAccessMemberService sharedAccessMemberService;
    private final CurrentUserService currentUserService;

    @Transactional
    public void create(SharedAccessAliasRequest request) throws LilanyusziException {
        User user = currentUserService.getAuthenticatedUser();
        Long sharedAccessId = request.sharedAccessId();

        SharedAccess sharedAccess = sharedAccessService.findById(sharedAccessId)
                .orElseThrow(() -> new LilanyusziException(SHARED_ACCESS_NOT_FOUND));

        if (!sharedAccessMemberService.existsBySharedAccessIdAndUserId(sharedAccessId, user.getId())) {
            throw new LilanyusziException(SHARED_ACCESS_ALIAS_ACCESS_DENIED);
        }

        SharedAccessAlias sharedAccessAlias = SharedAccessAlias.builder()
                .sharedAccess(sharedAccess)
                .user(user)
                .build();

        sharedAccessAlias.setAlias(request.alias().trim());
        save(sharedAccessAlias);
    }

    public SharedAccessAlias save(SharedAccessAlias sharedAccessAlias) {
        SharedAccessAlias savedSharedAccessAlias = sharedAccessAliasRepository.save(sharedAccessAlias);
        log.info("SAVED SHARED ACCESS ALIAS: {}", savedSharedAccessAlias);
        return savedSharedAccessAlias;
    }

    public Optional<String> findAliasBySharedAccessIdAndUserId(Long sharedAccessId, Long userId) {
        return sharedAccessAliasRepository.findBySharedAccessIdAndUserId(sharedAccessId, userId)
                .map(SharedAccessAlias::getAlias);
    }

    public void deleteBySharedAccessId(Long sharedAccessId) {
        sharedAccessAliasRepository.deleteBySharedAccessId(sharedAccessId);
        log.info("DELETED SHARED ACCESS ALIASES FOR SHARED ACCESS ID: {}", sharedAccessId);
    }

    public void deleteBySharedAccessIdAndUserId(Long sharedAccessId, Long userId) {
        sharedAccessAliasRepository.deleteBySharedAccessIdAndUserId(sharedAccessId, userId);
        log.info("DELETED SHARED ACCESS ALIAS FOR SHARED ACCESS ID: {}, USER ID: {}", sharedAccessId, userId);
    }
}
