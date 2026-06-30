package com.lilanyuszi.app.shared_access;

import com.lilanyuszi.app.api.LilanyusziException;
import com.lilanyuszi.app.shared_access_alias.SharedAccessAlias;
import com.lilanyuszi.app.shared_access_alias.SharedAccessAliasRepository;
import com.lilanyuszi.app.shared_access_member.SharedAccessMember;
import com.lilanyuszi.app.shared_access_member.SharedAccessMemberResponse;
import com.lilanyuszi.app.shared_access_member.SharedAccessMemberService;
import com.lilanyuszi.app.user.User;
import com.lilanyuszi.app.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static com.lilanyuszi.app.util.Constant.SHARED_ACCESS_EXISTS;

@Slf4j
@Service
@RequiredArgsConstructor
public class SharedAccessService {

    private static final String SHARED_ACCESS_UNIQUE_CONSTRAINT = "uk_shared_access_owner_type_canonical_name";

    private final SharedAccessRepository sharedAccessRepository;
    private final SharedAccessAliasRepository sharedAccessAliasRepository;
    private final SharedAccessMemberService sharedAccessMemberService;
    private final UserService userService;

    public SharedAccess save(SharedAccess sharedAccess) {
        SharedAccess savedSharedAccess = sharedAccessRepository.save(sharedAccess);
        log.info("SAVED SHARED ACCESS: {}", savedSharedAccess);
        return savedSharedAccess;
    }

    public SharedAccess create(User owner, SharedAccessType type, String name) throws LilanyusziException {
        String trimmedName = name.trim();
        String canonicalName = canonicalizeName(name);

        if (sharedAccessRepository.existsByOwnerUserIdAndTypeAndCanonicalName(
                owner.getId(),
                type,
                canonicalName
        )) {
            throw new LilanyusziException(SHARED_ACCESS_EXISTS);
        }

        try {
            SharedAccess savedSharedAccess = sharedAccessRepository.saveAndFlush(SharedAccess.builder()
                    .name(trimmedName)
                    .ownerUser(owner)
                    .type(type)
                    .canonicalName(canonicalName)
                    .build());
            log.info(
                    "CREATED SHARED ACCESS ID: {}, OWNER USER ID: {}, TYPE: {}",
                    savedSharedAccess.getId(),
                    owner.getId(),
                    type
            );
            return savedSharedAccess;
        } catch (DataIntegrityViolationException ex) {
            if (isSharedAccessUniqueConstraintViolation(ex)) {
                throw new LilanyusziException(SHARED_ACCESS_EXISTS);
            }
            throw ex;
        }
    }

    public Optional<SharedAccess> findById(Long id) {
        return sharedAccessRepository.findById(id);
    }

    public void deleteById(Long id) {
        sharedAccessRepository.deleteById(id);
        log.info("DELETED SHARED ACCESS ID: {}", id);
    }

    public List<SharedAccessResponse> findAllByUser() throws LilanyusziException {
        Long userId = userService.getAuthenticatedUserId();
        return sharedAccessMemberService.findByUserId(userId).stream()
                .map(sharedAccessMember -> toResponse(sharedAccessMember, userId))
                .toList();
    }

    private SharedAccessResponse toResponse(SharedAccessMember currentMember, Long userId) {
        SharedAccess sharedAccess = currentMember.getSharedAccess();
        String alias = sharedAccessAliasRepository
                .findBySharedAccessIdAndUserId(sharedAccess.getId(), userId)
                .map(SharedAccessAlias::getAlias)
                .orElse(sharedAccess.getName());
        List<SharedAccessMemberResponse> members = sharedAccessMemberService
                .findBySharedAccessId(sharedAccess.getId())
                .stream()
                .map(this::toMemberResponse)
                .toList();
        return new SharedAccessResponse(
                sharedAccess.getId(),
                sharedAccess.getName(),
                alias,
                sharedAccess.getCreatedAt(),
                sharedAccess.getUpdatedAt(),
                sharedAccess.getType(),
                roleOf(sharedAccess, currentMember.getUser().getId()),
                members,
                isOwner(sharedAccess, currentMember.getUser().getId())
        );
    }

    private SharedAccessMemberResponse toMemberResponse(SharedAccessMember sharedAccessMember) {
        SharedAccess sharedAccess = sharedAccessMember.getSharedAccess();
        return new SharedAccessMemberResponse(
                sharedAccessMember.getUser().getName(),
                roleOf(sharedAccess, sharedAccessMember.getUser().getId()),
                sharedAccessMember.getUser().getId()
        );
    }

    private SharedAccessRole roleOf(SharedAccess sharedAccess, Long userId) {
        return isOwner(sharedAccess, userId)
                ? SharedAccessRole.OWNER
                : SharedAccessRole.MEMBER;
    }

    private boolean isOwner(SharedAccess sharedAccess, Long userId) {
        return sharedAccess.getOwnerUser().getId().equals(userId);
    }

    private String canonicalizeName(String name) {
        return Normalizer.normalize(name.trim(), Normalizer.Form.NFC)
                .toLowerCase(Locale.ROOT);
    }

    private boolean isSharedAccessUniqueConstraintViolation(DataIntegrityViolationException ex) {
        Throwable current = ex;
        while (current != null) {
            if (current.getMessage() != null
                    && current.getMessage().contains(SHARED_ACCESS_UNIQUE_CONSTRAINT)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
