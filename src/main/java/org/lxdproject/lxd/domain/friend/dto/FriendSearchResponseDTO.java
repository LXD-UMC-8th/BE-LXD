package org.lxdproject.lxd.domain.friend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.lxdproject.lxd.global.common.dto.PageDTO;

@Getter
@Builder
public class FriendSearchResponseDTO {

    private String query;
    private PageDTO<MemberInfo> members;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class MemberInfo {
        private Long memberId;
        private String username;
        private String nickname;
        private String profileImageUrl;
    }
}
