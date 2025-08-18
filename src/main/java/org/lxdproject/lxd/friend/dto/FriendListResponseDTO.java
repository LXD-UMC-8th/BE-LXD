package org.lxdproject.lxd.friend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.lxdproject.lxd.common.dto.PageDTO;

@Getter
@AllArgsConstructor
public class FriendListResponseDTO {
    private int totalRequests;
    private PageDTO<FriendResponseDTO> friends;
}