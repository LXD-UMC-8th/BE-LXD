package org.lxdproject.lxd.domain.friend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.lxdproject.lxd.global.common.dto.PageDTO;

@Getter
@AllArgsConstructor
public class FriendListResponseDTO {
    private int totalRequests;
    private PageDTO<FriendResponseDTO> friends;
}