package org.lxdproject.lxd.friend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.lxdproject.lxd.common.dto.PageResponse;

@Getter
@AllArgsConstructor
public class FriendRequestListResponseDTO {
    private Long totalFriends;
    private PageResponse<FriendResponseDTO> sentRequests;
    private PageResponse<FriendResponseDTO> receivedRequests;
}
