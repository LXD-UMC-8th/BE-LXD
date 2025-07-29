package org.lxdproject.lxd.member.repository;

import org.lxdproject.lxd.member.entity.FriendRequest;
import org.lxdproject.lxd.member.entity.Member;
import org.lxdproject.lxd.member.entity.enums.FriendRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    boolean existsByRequesterAndReceiverAndStatus(Member requester, Member receiver, FriendRequestStatus status);
    Optional<FriendRequest> findByRequesterIdAndReceiverId(Long requesterId, Long receiverId);
}