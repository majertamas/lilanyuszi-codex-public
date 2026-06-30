package com.lilanyuszi.app.shared_access_member;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SharedAccessMemberService {

    private final SharedAccessMemberRepository sharedAccessMemberRepository;

    public SharedAccessMember save(SharedAccessMember sharedAccessMember) {
        SharedAccessMember savedSharedAccessMember = sharedAccessMemberRepository.save(sharedAccessMember);
        log.info("SAVED SHARED ACCESS MEMBER: {}", savedSharedAccessMember);
        return savedSharedAccessMember;
    }

    public List<SharedAccessMember> findByUserId(Long userId) {
        return sharedAccessMemberRepository.findByUserId(userId);
    }

    public List<SharedAccessMember> findBySharedAccessId(Long sharedAccessId) {
        return sharedAccessMemberRepository.findBySharedAccessId(sharedAccessId);
    }

    public boolean existsBySharedAccessIdAndUserId(Long sharedAccessId, Long userId) {
        return sharedAccessMemberRepository.existsBySharedAccessIdAndUserId(sharedAccessId, userId);
    }

    public void deleteBySharedAccessId(Long sharedAccessId) {
        sharedAccessMemberRepository.deleteBySharedAccessId(sharedAccessId);
        log.info("DELETED SHARED ACCESS MEMBERS FOR SHARED ACCESS ID: {}", sharedAccessId);
    }

    public void deleteBySharedAccessIdAndUserId(Long sharedAccessId, Long userId) {
        sharedAccessMemberRepository.deleteBySharedAccessIdAndUserId(sharedAccessId, userId);
        log.info("DELETED SHARED ACCESS MEMBER FOR SHARED ACCESS ID: {}, USER ID: {}", sharedAccessId, userId);
    }
}
