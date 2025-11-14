package org.lxdproject.lxd.global.common.dto;

import lombok.Builder;
import org.lxdproject.lxd.domain.member.entity.Member;

@Builder
public record MemberProfileDTO(Long id, String username, String nickname, String profileImage) {
    public static MemberProfileDTO from(Member member) {
        if (member == null || member.isDeleted()) {
            return new MemberProfileDTO(null, null, "탈퇴한 사용자", null);
        }
        return new MemberProfileDTO(
                member.getId(),
                member.getUsername(),
                member.getNickname(),
                member.getProfileImg()
        );
    }
}
