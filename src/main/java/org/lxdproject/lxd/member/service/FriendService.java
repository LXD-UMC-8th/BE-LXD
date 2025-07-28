package org.lxdproject.lxd.member.service;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.member.dto.FriendListResponseDTO;
import org.lxdproject.lxd.member.dto.FriendResponseDTO;
import org.lxdproject.lxd.member.entity.Member;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendService {
    public FriendListResponseDTO getFriendList(Long memberId) {
        List<Member> friends = friendRepository.findFriendsByMemberId(memberId);

        List<FriendResponseDTO> friendDtos = friends.stream()
                .map(friend -> new FriendResponseDTO(friend.getId(), friend.getUsername(), friend.getNickname()))
                .collect(Collectors.toList());

        return new FriendListResponseDTO(friendDtos.size(), friendDtos);
    }}
