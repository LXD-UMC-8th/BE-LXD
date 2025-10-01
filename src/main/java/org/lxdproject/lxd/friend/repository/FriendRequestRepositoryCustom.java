package org.lxdproject.lxd.friend.repository;

import org.lxdproject.lxd.friend.dto.FriendResponseDTO;
import org.lxdproject.lxd.friend.entity.enums.FriendRequestStatus;
import org.lxdproject.lxd.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FriendRequestRepositoryCustom {
    Page<FriendResponseDTO> findReceivedRequestDTOs(Member receiver, FriendRequestStatus status, Pageable pageable);
    Page<FriendResponseDTO> findSentRequestDTOs(Member requester, FriendRequestStatus status, Pageable pageable);
}
