package org.lxdproject.lxd.domain.friend.repository;

import org.lxdproject.lxd.domain.friend.entity.FriendRequest;
import org.lxdproject.lxd.domain.member.entity.Member;
import org.lxdproject.lxd.domain.friend.entity.enums.FriendRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long>, FriendRequestRepositoryCustom {
    boolean existsByRequesterAndReceiverAndStatus(Member requester, Member receiver, FriendRequestStatus status);
    Optional<FriendRequest> findByRequesterIdAndReceiverId(Long requesterId, Long receiverId);
    Optional<FriendRequest> findByRequesterIdAndReceiverIdAndStatus(Long requesterId, Long receiverId, FriendRequestStatus status);

    int countByRequesterAndStatus(Member requester, FriendRequestStatus status);
    int countByReceiverAndStatus(Member receiver, FriendRequestStatus status);
}