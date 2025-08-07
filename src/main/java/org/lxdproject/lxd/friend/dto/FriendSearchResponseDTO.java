package org.lxdproject.lxd.friend.dto;

import lombok.Builder;
import lombok.Getter;
import org.lxdproject.lxd.common.dto.PageResponse;

@Getter
@Builder
public class FriendSearchResponseDTO {

    private String query;
    private PageResponse<MemberInfo> members;

    @Getter
    @Builder
    public static class MemberInfo {
        private Long memberId;
        private String username;
        private String nickname;
        private String profileImageUrl;
    }
}
