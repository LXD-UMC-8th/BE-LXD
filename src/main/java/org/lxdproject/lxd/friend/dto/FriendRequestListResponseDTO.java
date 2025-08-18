package org.lxdproject.lxd.friend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.lxdproject.lxd.common.dto.PageDTO;

@Getter
@AllArgsConstructor
public class FriendRequestListResponseDTO {
    private Long totalFriends;
    private PageDTO<FriendResponseDTO> sentRequests;
    private PageDTO<FriendResponseDTO> receivedRequests;
}
