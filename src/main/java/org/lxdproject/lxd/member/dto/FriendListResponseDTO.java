package org.lxdproject.lxd.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.lxdproject.lxd.common.dto.PageResponse;

import java.util.List;

@Getter
@AllArgsConstructor
public class FriendListResponseDTO {
    private int totalRequests;
    private PageResponse<FriendResponseDTO> friends;
}