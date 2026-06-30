package com.lilanyuszi.app.shared_access;

import com.lilanyuszi.app.api.LilanyusziException;
import com.lilanyuszi.app.shared_access_member.SharedAccessMemberService;
import com.lilanyuszi.app.user.CurrentUserService;
import com.lilanyuszi.app.user.User;
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
    private final SharedAccessResponseMapper sharedAccessResponseMapper;
    private final SharedAccessMemberService sharedAccessMemberService;
    private final CurrentUserService currentUserService;

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
        Long userId = currentUserService.getAuthenticatedUserId();
        return sharedAccessMemberService.findByUserId(userId).stream()
                .map(sharedAccessMember -> sharedAccessResponseMapper.toResponse(sharedAccessMember, userId))
                .toList();
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
