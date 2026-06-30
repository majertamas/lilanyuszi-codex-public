package com.lilanyuszi.app.shared_access_alias;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SharedAccessAliasRepository extends JpaRepository<SharedAccessAlias, Long> {

    Optional<SharedAccessAlias> findByIdAndUserId(Long id, Long userId);

    Optional<SharedAccessAlias> findBySharedAccessIdAndUserId(Long sharedAccessId, Long userId);

    void deleteBySharedAccessId(Long sharedAccessId);

    void deleteBySharedAccessIdAndUserId(Long sharedAccessId, Long userId);
}
