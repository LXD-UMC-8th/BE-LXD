package org.lxdproject.lxd.domain.friend.repository;

import org.lxdproject.lxd.domain.friend.dto.FriendResponseDTO;
import org.lxdproject.lxd.domain.friend.entity.enums.FriendRequestStatus;
import org.lxdproject.lxd.domain.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface FriendRequestRepositoryCustom {
    Page<FriendResponseDTO> findReceivedRequestDTOs(Member receiver, FriendRequestStatus status, Pageable pageable);
    Page<FriendResponseDTO> findSentRequestDTOs(Member requester, FriendRequestStatus status, Pageable pageable);
    void hardDeleteFriendRequestsOlderThanThreshold(LocalDateTime threshold);
}
