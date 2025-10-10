package org.lxdproject.lxd.friend.repository;

import org.lxdproject.lxd.friend.dto.FriendResponseDTO;
import org.lxdproject.lxd.friend.entity.FriendRequest;
import org.lxdproject.lxd.member.entity.Member;
import org.lxdproject.lxd.friend.entity.enums.FriendRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long>, FriendRequestRepositoryCustom {
    boolean existsByRequesterAndReceiverAndStatus(Member requester, Member receiver, FriendRequestStatus status);
    Optional<FriendRequest> findByRequesterIdAndReceiverId(Long requesterId, Long receiverId);
    Optional<FriendRequest> findByRequesterIdAndReceiverIdAndStatus(Long requesterId, Long receiverId, FriendRequestStatus status);

    int countByRequesterAndStatus(Member requester, FriendRequestStatus status);
    int countByReceiverAndStatus(Member receiver, FriendRequestStatus status);
}