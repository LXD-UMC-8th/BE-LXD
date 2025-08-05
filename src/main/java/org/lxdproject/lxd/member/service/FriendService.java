package org.lxdproject.lxd.member.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.code.exception.handler.FriendHandler;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;

import org.lxdproject.lxd.config.security.SecurityUtil;
import org.lxdproject.lxd.member.dto.*;
import org.lxdproject.lxd.member.entity.FriendRequest;
import org.lxdproject.lxd.member.entity.Member;
import org.lxdproject.lxd.member.entity.enums.FriendRequestStatus;
import org.lxdproject.lxd.member.repository.FriendRepository;
import org.lxdproject.lxd.member.repository.FriendRequestRepository;
import org.lxdproject.lxd.member.repository.MemberRepository;
import org.lxdproject.lxd.notification.dto.NotificationRequestDTO;
import org.lxdproject.lxd.notification.entity.enums.NotificationType;
import org.lxdproject.lxd.notification.entity.enums.TargetType;
import org.lxdproject.lxd.notification.service.NotificationService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendRepository friendRepository;
    private final MemberRepository memberRepository;
    private final FriendRequestRepository friendRequestRepository;

    private final NotificationService notificationService;

    public FriendListResponseDTO getFriendList(Long memberId) {
        Member member = findMemberById(memberId);

        List<Member> friends = getFriends(memberId);
        int totalFriends = friends.size();

        int totalRequests = getSentRequestCount(member) + getReceivedRequestCount(member);

        List<FriendResponseDTO> friendDtos = friends.stream()
                .map(friend -> new FriendResponseDTO(
                        friend.getId(),
                        friend.getUsername(),
                        friend.getNickname(),
                        friend.getProfileImg()))
                .toList();

        return new FriendListResponseDTO(totalFriends, totalRequests, friendDtos);
    }

    public void sendFriendRequest(Long requesterId, FriendRequestCreateRequestDTO requestDto) {
        Long receiverId = requestDto.getReceiverId();

        if (requesterId.equals(receiverId)) {
            throw new FriendHandler(ErrorStatus.INVALID_FRIEND_REQUEST);
        }

        Member requester = memberRepository.findById(requesterId)
                .orElseThrow(() -> new FriendHandler(ErrorStatus.MEMBER_NOT_FOUND));

        Member receiver = memberRepository.findById(receiverId)
                .orElseThrow(() -> new FriendHandler(ErrorStatus.MEMBER_NOT_FOUND));

        boolean alreadyRequested = friendRequestRepository.existsByRequesterAndReceiverAndStatus(
                requester, receiver, FriendRequestStatus.PENDING);
        // 양방향 친구 요청 확인 추가
        boolean reverseRequested = friendRequestRepository.existsByRequesterAndReceiverAndStatus(
                receiver, requester, FriendRequestStatus.PENDING);
        if (alreadyRequested || reverseRequested) {
            throw new FriendHandler(ErrorStatus.FRIEND_REQUEST_ALREADY_SENT);
        }

        boolean alreadyFriends = friendRepository.existsByRequesterAndReceiverOrReceiverAndRequester(requester, receiver);
        if (alreadyFriends) {
            throw new FriendHandler(ErrorStatus.ALREADY_FRIENDS);
        }

        FriendRequest friendRequest = FriendRequest.builder()
                .requester(requester)
                .receiver(receiver)
                .status(FriendRequestStatus.PENDING)
                .build();

        friendRequestRepository.save(friendRequest);

        NotificationRequestDTO dto = NotificationRequestDTO.builder()
                .receiverId(receiver.getId())
                .notificationType(NotificationType.FRIEND_REQUEST)
                .targetType(TargetType.MEMBER)
                .targetId(receiver.getId())
                .redirectUrl("/members/" + requester.getId())
                .build();

        notificationService.saveAndPublishNotification(dto);
    }

    public void acceptFriendRequest(Long receiverId, FriendRequestAcceptRequestDTO requestDto) {
        Long requesterId = requestDto.getRequesterId();

        if (receiverId.equals(requesterId)) {
            throw new FriendHandler(ErrorStatus.INVALID_FRIEND_REQUEST);
        }

        FriendRequest request = friendRequestRepository.findByRequesterIdAndReceiverId(requesterId, receiverId)
                .orElseThrow(() -> new FriendHandler(ErrorStatus.FRIEND_NOT_FOUND));

        if (!request.getStatus().equals(FriendRequestStatus.PENDING)) {
            throw new FriendHandler(ErrorStatus.FRIEND_REQUEST_NOT_PENDING);
        }

        // 요청 상태 변경
        request.accept();

        // 친구 관계 저장 (양방향)
        Member requester = request.getRequester();
        Member receiver = request.getReceiver();
        friendRepository.saveFriendship(requester, receiver);
        friendRepository.saveFriendship(receiver, requester); // 양방향 저장

        NotificationRequestDTO dto = NotificationRequestDTO.builder()
                .receiverId(requester.getId()) // 친구 요청 보낸 사람에게 알림 전송
                .notificationType(NotificationType.FRIEND_ACCEPTED)
                .targetType(TargetType.MEMBER)
                .targetId(receiver.getId()) // 친구 요청 수락한 사람
                .redirectUrl("/members/" + receiver.getId())
                .build();

        notificationService.saveAndPublishNotification(dto);
    }

    public void deleteFriend(Long currentMemberId, Long friendId) {
        Member current = memberRepository.findById(currentMemberId)
                .orElseThrow(() -> new FriendHandler(ErrorStatus.MEMBER_NOT_FOUND));
        Member target = memberRepository.findById(friendId)
                .orElseThrow(() -> new FriendHandler(ErrorStatus.MEMBER_NOT_FOUND));

        // 존재 여부 확인
        boolean exists = friendRepository.existsByRequesterAndReceiverOrReceiverAndRequester(current, target);
        if (!exists) {
            throw new FriendHandler(ErrorStatus.NOT_FRIEND);
        }

        // soft delete
        friendRepository.softDeleteFriendship(current, target);
    }

    public FriendRequestListResponseDTO getPendingFriendRequests(Long memberId) {
        Member currentMember = findMemberById(memberId);

        List<FriendRequest> sent = friendRequestRepository.findByRequesterAndStatus(currentMember, FriendRequestStatus.PENDING);
        List<FriendRequest> received = friendRequestRepository.findByReceiverAndStatus(currentMember, FriendRequestStatus.PENDING);

        int sentCount = sent.size();
        int receivedCount = received.size();
        int totalFriends = getFriends(memberId).size();

        List<FriendResponseDTO> sentDtos = sent.stream()
                .map(req -> mapToDto(req.getReceiver()))
                .toList();

        List<FriendResponseDTO> receivedDtos = received.stream()
                .map(req -> mapToDto(req.getRequester()))
                .toList();

        return new FriendRequestListResponseDTO(
                sentCount,
                receivedCount,
                totalFriends,
                sentDtos,
                receivedDtos
        );
    }

    private Member findMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new FriendHandler(ErrorStatus.MEMBER_NOT_FOUND));
    }

    private List<Member> getFriends(Long memberId) {
        return friendRepository.findFriendsByMemberId(memberId);
    }

    private int getSentRequestCount(Member member) {
        return friendRequestRepository
                .findByRequesterAndStatus(member, FriendRequestStatus.PENDING)
                .size();
    }

    private int getReceivedRequestCount(Member member) {
        return friendRequestRepository
                .findByReceiverAndStatus(member, FriendRequestStatus.PENDING)
                .size();
    }

    private FriendResponseDTO mapToDto(Member member) {
        return new FriendResponseDTO(
                member.getId(),
                member.getUsername(),
                member.getNickname(),
                member.getProfileImg()
        );
    }

    @Transactional
    public void refuseFriendRequest(FriendRequestRefuseRequestDTO requestDto) {
        Long receiverId = SecurityUtil.getCurrentMemberId();
        Member receiver = memberRepository.findById(receiverId)
                .orElseThrow(() -> new FriendHandler(ErrorStatus.MEMBER_NOT_FOUND));

        FriendRequest friendRequest = friendRequestRepository
                .findByRequesterIdAndReceiverIdAndStatus(requestDto.getRequesterId(), receiverId, FriendRequestStatus.PENDING)
                .orElseThrow(() -> new FriendHandler(ErrorStatus.FRIEND_REQUEST_NOT_FOUND));

        if (!friendRequest.getStatus().equals(FriendRequestStatus.PENDING)) {
            throw new FriendHandler(ErrorStatus.INVALID_FRIEND_REQUEST_STATUS);
        }

        friendRequest.reject(); // 상태 변경
        friendRequestRepository.save(friendRequest);
    }

    @Transactional
    public void cancelFriendRequest(FriendRequestCancelRequestDTO requestDto) {
        Long requesterId = SecurityUtil.getCurrentMemberId();
        Member requester = memberRepository.findById(requesterId)
                .orElseThrow(() -> new FriendHandler(ErrorStatus.MEMBER_NOT_FOUND));

        FriendRequest friendRequest = friendRequestRepository
                .findByRequesterIdAndReceiverIdAndStatus(requesterId, requestDto.getReceiverId(), FriendRequestStatus.PENDING)
                .orElseThrow(() -> new FriendHandler(ErrorStatus.FRIEND_REQUEST_NOT_FOUND));

        if (!friendRequest.getStatus().equals(FriendRequestStatus.PENDING)) {
            throw new FriendHandler(ErrorStatus.INVALID_FRIEND_REQUEST_STATUS);
        }

        friendRequest.cancel(); // 상태 변경
        friendRequestRepository.save(friendRequest);
    }
}
