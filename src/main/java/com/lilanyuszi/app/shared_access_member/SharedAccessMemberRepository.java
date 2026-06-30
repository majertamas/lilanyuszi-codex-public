package com.lilanyuszi.app.shared_access_member;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SharedAccessMemberRepository extends JpaRepository<SharedAccessMember, Long> {
    List<SharedAccessMember> findByUserId(Long userId);

    List<SharedAccessMember> findBySharedAccessId(Long sharedAccessId);

    boolean existsBySharedAccessIdAndUserId(Long sharedAccessId, Long userId);

    void deleteBySharedAccessId(Long sharedAccessId);

    void deleteBySharedAccessIdAndUserId(Long sharedAccessId, Long userId);
}
