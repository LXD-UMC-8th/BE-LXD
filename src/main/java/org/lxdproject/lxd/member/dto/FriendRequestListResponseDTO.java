package org.lxdproject.lxd.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class FriendRequestListResponseDTO {
    private int sentRequestsCount;
    private int receivedRequestsCount;
    private List<FriendResponseDTO> sentRequests;
    private List<FriendResponseDTO> receivedRequests;
}
