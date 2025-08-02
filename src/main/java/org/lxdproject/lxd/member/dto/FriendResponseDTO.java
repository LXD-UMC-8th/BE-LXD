package org.lxdproject.lxd.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FriendResponseDTO {
    private Long memberId;
    private String username;
    private String nickname;
    private String profileImg;
}