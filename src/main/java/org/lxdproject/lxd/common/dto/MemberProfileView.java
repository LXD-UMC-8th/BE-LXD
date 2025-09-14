package org.lxdproject.lxd.common.dto;

import lombok.Builder;
import lombok.Getter;
import org.lxdproject.lxd.member.entity.Member;

@Builder
public record MemberProfileView(Long id, String username, String nickname, String profileImage) {
    public static MemberProfileView from(Member member) {
        if (member.isDeleted()) {
            return new MemberProfileView(null, null, "탈퇴한 사용자", null);
        }
        return new MemberProfileView(
                member.getId(),
                member.getUsername(),
                member.getNickname(),
                member.getProfileImg()
        );
    }
}
