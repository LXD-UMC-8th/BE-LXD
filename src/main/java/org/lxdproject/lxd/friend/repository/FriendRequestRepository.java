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

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    boolean existsByRequesterAndReceiverAndStatus(Member requester, Member receiver, FriendRequestStatus status);
    Optional<FriendRequest> findByRequesterIdAndReceiverId(Long requesterId, Long receiverId);
    Optional<FriendRequest> findByRequesterIdAndReceiverIdAndStatus(Long requesterId, Long receiverId, FriendRequestStatus status);

    int countByRequesterAndStatus(Member requester, FriendRequestStatus status);
    int countByReceiverAndStatus(Member receiver, FriendRequestStatus status);

    @Query("""
SELECT new org.lxdproject.lxd.friend.dto.FriendResponseDTO(
    m.id,
    m.username,
    m.nickname,
    m.profileImg
)
FROM FriendRequest fr
JOIN fr.requester m
WHERE fr.receiver = :receiver AND fr.status = :status
""")
    Page<FriendResponseDTO> findReceivedRequestDTOs(@Param("receiver") Member receiver, @Param("status") FriendRequestStatus status, Pageable pageable);
    @Query("""
SELECT new org.lxdproject.lxd.friend.dto.FriendResponseDTO(
    m.id,
    m.username,
    m.nickname,
    m.profileImg
)
FROM FriendRequest fr
JOIN fr.receiver m
WHERE fr.requester = :requester AND fr.status = :status
""")
    Page<FriendResponseDTO> findSentRequestDTOs(@Param("requester") Member requester, @Param("status") FriendRequestStatus status, Pageable pageable);

}