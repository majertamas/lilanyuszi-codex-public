package com.lilanyuszi.app.shared_access;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SharedAccessRepository extends JpaRepository<SharedAccess, Long> {
    boolean existsByOwnerUserIdAndTypeAndCanonicalName(
            Long ownerUserId,
            SharedAccessType type,
            String canonicalName
    );
}
