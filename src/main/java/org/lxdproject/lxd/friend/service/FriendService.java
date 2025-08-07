package org.lxdproject.lxd.friend.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lxdproject.lxd.apiPayload.code.exception.handler.FriendHandler;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;

import org.lxdproject.lxd.common.dto.PageResponse;
import org.lxdproject.lxd.config.security.SecurityUtil;
import org.lxdproject.lxd.friend.dto.*;
import org.lxdproject.lxd.friend.entity.FriendRequest;
import org.lxdproject.lxd.infra.redis.RedisKeyPrefix;
import org.lxdproject.lxd.infra.redis.RedisService;
import org.lxdproject.lxd.member.entity.Member;
import org.lxdproject.lxd.friend.entity.enums.FriendRequestStatus;
import org.lxdproject.lxd.friend.repository.FriendRepository;
import org.lxdproject.lxd.friend.repository.FriendRequestRepository;
import org.lxdproject.lxd.member.repository.MemberRepository;
import org.lxdproject.lxd.notification.dto.NotificationRequestDTO;
import org.lxdproject.lxd.notification.entity.enums.NotificationType;
import org.lxdproject.lxd.notification.entity.enums.TargetType;
import org.lxdproject.lxd.notification.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class FriendService {

    private final FriendRepository friendRepository;
    private final MemberRepository memberRepository;
    private final FriendRequestRepository friendRequestRepository;

    private final NotificationService notificationService;
    private final RedisService redisService;

    @Transactional(readOnly = true)
    public FriendListResponseDTO getFriendList(Long memberId, Pageable pageable) {
        Member member = findMemberById(memberId);

        Page<Member> friendsPage = getFriends(memberId, pageable);

        int totalRequests = getSentRequestCount(member) + getReceivedRequestCount(member);

        List<FriendResponseDTO> friendDtos = friendsPage.stream()
                .map(friend -> new FriendResponseDTO(
                        friend.getId(),
                        friend.getUsername(),
                        friend.getNickname(),
                        friend.getProfileImg()))
                .toList();

        PageResponse<FriendResponseDTO> pageResponse = new PageResponse<>(
                friendsPage.getTotalElements(), // 또는 -1
                friendDtos,
                friendsPage.getNumber() + 1,     // 0-based → 1-based
                friendsPage.getSize(),
                friendsPage.hasNext()
        );

        return new FriendListResponseDTO(totalRequests, pageResponse);
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

        boolean reverseRequested = friendRequestRepository.existsByRequesterAndReceiverAndStatus(
                receiver, requester, FriendRequestStatus.PENDING);
        if (alreadyRequested || reverseRequested) {
            throw new FriendHandler(ErrorStatus.FRIEND_REQUEST_ALREADY_SENT);
        }

        boolean alreadyFriends = friendRepository.existsFriendshipByRequesterAndReceiver(requester, receiver);
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
                .targetId(requester.getId())
                .redirectUrl("/members/" + requester.getId())
                .build();

        notificationService.saveAndPublishNotification(dto);
    }

    @Transactional
    public void acceptFriendRequest(Long receiverId, FriendRequestAcceptRequestDTO requestDto) {
        Long requesterId = requestDto.getRequesterId();
        FriendRequest request = friendRequestRepository
                .findByRequesterIdAndReceiverId(requesterId, receiverId)
                .orElseThrow(() -> new FriendHandler(ErrorStatus.FRIEND_NOT_FOUND));

        if (request.getStatus() != FriendRequestStatus.PENDING) {
            throw new FriendHandler(ErrorStatus.FRIEND_REQUEST_NOT_PENDING);
        }

        // 친구 관계 양방향 저장
        Member requester = request.getRequester();
        Member receiver = request.getReceiver();
        friendRepository.saveFriendship(requester, receiver);

        friendRequestRepository.delete(request);

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

        boolean exists = friendRepository.existsFriendshipByRequesterAndReceiver(current, target);
        if (!exists) {
            throw new FriendHandler(ErrorStatus.NOT_FRIEND);
        }

        friendRepository.softDeleteFriendship(current, target);
    }

    @Transactional(readOnly = true)
    public FriendRequestListResponseDTO getPendingFriendRequests(Long memberId, Pageable receivedPage, Pageable sentPage) {
        Member currentMember = findMemberById(memberId);

        Page<FriendResponseDTO> sent = friendRequestRepository
                .findSentRequestDTOs(currentMember, FriendRequestStatus.PENDING, sentPage);

        Page<FriendResponseDTO> received = friendRequestRepository
                .findReceivedRequestDTOs(currentMember, FriendRequestStatus.PENDING, receivedPage);

        Long totalFriends = friendRepository.countFriendsByMemberId(memberId);

        return new FriendRequestListResponseDTO(
                totalFriends,
                new PageResponse<>(
                        sent.getTotalElements(),
                        sent.getContent(),
                        sent.getNumber() + 1,
                        sent.getSize(),
                        sent.hasNext()
                ),
                new PageResponse<>(
                        received.getTotalElements(),
                        received.getContent(),
                        received.getNumber() + 1,
                        received.getSize(),
                        received.hasNext()
                )
        );
    }

    private Member findMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new FriendHandler(ErrorStatus.MEMBER_NOT_FOUND));
    }

    private Page<Member> getFriends(Long memberId, Pageable pageable) {
        return friendRepository.findFriendsByMemberId(memberId, pageable);
    }

    private int getSentRequestCount(Member member) {
        return friendRequestRepository.countByRequesterAndStatus(member, FriendRequestStatus.PENDING);
    }

    private int getReceivedRequestCount(Member member) {
        return friendRequestRepository.countByReceiverAndStatus(member, FriendRequestStatus.PENDING);
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
        FriendRequest request = friendRequestRepository
                .findByRequesterIdAndReceiverIdAndStatus(requestDto.getRequesterId(), SecurityUtil.getCurrentMemberId(), FriendRequestStatus.PENDING)
                .orElseThrow(() -> new FriendHandler(ErrorStatus.FRIEND_REQUEST_NOT_FOUND));

        friendRequestRepository.delete(request);
    }

    @Transactional
    public void cancelFriendRequest(FriendRequestCancelRequestDTO requestDto) {
        FriendRequest request = friendRequestRepository
                .findByRequesterIdAndReceiverIdAndStatus(SecurityUtil.getCurrentMemberId(), requestDto.getReceiverId(), FriendRequestStatus.PENDING)
                .orElseThrow(() -> new FriendHandler(ErrorStatus.FRIEND_REQUEST_NOT_FOUND));

        friendRequestRepository.delete(request);
    }

    public FriendSearchResponseDTO searchFriends(Long memberId, String query, Pageable pageable) {
        if (query.isBlank()) {
            return FriendSearchResponseDTO.builder()
                    .query(query)
                    .members(PageResponse.<FriendSearchResponseDTO.MemberInfo>builder()
                            .contents(Collections.emptyList())
                            .page(pageable.getPageNumber())
                            .size(pageable.getPageSize())
                            .hasNext(false)
                            .build())
                    .build();
        }

        // 친구 ID 목록
        Set<Long> friendIds = friendRepository.findFriendIdsByMemberId(memberId);

        // 검색 결과 조회
        Page<FriendSearchResponseDTO.MemberInfo> resultPage = memberRepository.searchByQuery(query, memberId, friendIds, pageable);

        // 검색 기록 redis에 저장
        saveRecentSearchKeyword(memberId, query);

        return FriendSearchResponseDTO.builder()
                .query(query)
                .members(PageResponse.<FriendSearchResponseDTO.MemberInfo>builder()
                        .contents(resultPage.getContent())
                        .page(resultPage.getNumber())
                        .size(resultPage.getSize())
                        .hasNext(resultPage.hasNext())
                        .build())
                .build();
    }

    private void saveRecentSearchKeyword(Long memberId, String query) {
        try {
            String key = RedisKeyPrefix.recentFriendSearchKey(memberId);
            redisService.pushRecentSearchKeyword(key, query, 10);
        } catch (Exception e) {
            log.warn("[FailedToSaveSearchKeyword] 멤버의 검색 기록 저장에 실패했습니다. {}: {}", memberId, e.getMessage());
        }
    }

    public List<String> getRecentSearchKeywords(Long memberId, int limit) {
        String key = RedisKeyPrefix.recentFriendSearchKey(memberId);
        return redisService.getRecentSearchKeywords(key, limit);
    }

    public void deleteKeyword(Long memberId, String query) {
        String key = RedisKeyPrefix.recentFriendSearchKey(memberId);
        redisService.removeListValue(key, query);
    }

    public void clearKeywords(Long memberId) {
        String key = RedisKeyPrefix.recentFriendSearchKey(memberId);
        redisService.deleteValues(key);
    }

}
