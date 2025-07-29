package org.lxdproject.lxd.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class FriendListResponseDTO {
    private int totalFriends;
    private List<FriendResponseDTO> friends;
}