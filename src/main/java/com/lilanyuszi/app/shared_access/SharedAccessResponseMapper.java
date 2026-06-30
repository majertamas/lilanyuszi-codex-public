package com.lilanyuszi.app.shared_access;

import com.lilanyuszi.app.shared_access_alias.SharedAccessAlias;
import com.lilanyuszi.app.shared_access_alias.SharedAccessAliasRepository;
import com.lilanyuszi.app.shared_access_member.SharedAccessMember;
import com.lilanyuszi.app.shared_access_member.SharedAccessMemberResponse;
import com.lilanyuszi.app.shared_access_member.SharedAccessMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SharedAccessResponseMapper {

    private final SharedAccessAliasRepository sharedAccessAliasRepository;
    private final SharedAccessMemberService sharedAccessMemberService;

    public SharedAccessResponse toResponse(SharedAccessMember currentMember, Long userId) {
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
}
