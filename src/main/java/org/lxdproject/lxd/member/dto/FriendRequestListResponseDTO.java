package org.lxdproject.lxd.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.lxdproject.lxd.common.dto.PageResponse;

import java.util.List;

@Getter
@AllArgsConstructor
public class FriendRequestListResponseDTO {
    private Long totalFriends;
    private PageResponse<FriendResponseDTO> sentRequests;
    private PageResponse<FriendResponseDTO> receivedRequests;
}
