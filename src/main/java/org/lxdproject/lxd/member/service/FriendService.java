package org.lxdproject.lxd.member.service;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.code.exception.handler.FriendHandler;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.diary.dto.MyDiarySummaryResponseDTO;
import org.lxdproject.lxd.diary.entity.Diary;
import org.lxdproject.lxd.diary.repository.DiaryRepository;
import org.lxdproject.lxd.member.dto.*;
import org.lxdproject.lxd.member.entity.FriendRequest;
import org.lxdproject.lxd.member.entity.Member;
import org.lxdproject.lxd.member.entity.enums.FriendRequestStatus;
import org.lxdproject.lxd.member.repository.FriendRepository;
import org.lxdproject.lxd.member.repository.FriendRequestRepository;
import org.lxdproject.lxd.member.repository.MemberRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static org.lxdproject.lxd.diary.util.DiaryUtil.generateContentPreview;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendRepository friendRepository;
    private final MemberRepository memberRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final DiaryRepository diaryRepository;

    public FriendListResponseDTO getFriendList(Long memberId) {

        List<Member> friends = friendRepository.findFriendsByMemberId(memberId);

        List<FriendResponseDTO> friendDtos = friends.stream()
                .map(friend -> new FriendResponseDTO(friend.getId(), friend.getUsername(), friend.getNickname()))
                .collect(Collectors.toList());

        return new FriendListResponseDTO(friendDtos.size(), friendDtos);
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
        if (alreadyRequested) {
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
        Member currentMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new FriendHandler(ErrorStatus.MEMBER_NOT_FOUND));

        List<FriendRequest> sent = friendRequestRepository.findByRequesterAndStatus(currentMember, FriendRequestStatus.PENDING);
        List<FriendRequest> received = friendRequestRepository.findByReceiverAndStatus(currentMember, FriendRequestStatus.PENDING);

        List<FriendResponseDTO> sentDtos = sent.stream()
                .map(req -> {
                    Member target = req.getReceiver();
                    return new FriendResponseDTO(target.getId(), target.getUsername(), target.getNickname());
                }).toList();

        List<FriendResponseDTO> receivedDtos = received.stream()
                .map(req -> {
                    Member target = req.getRequester();
                    return new FriendResponseDTO(target.getId(), target.getUsername(), target.getNickname());
                }).toList();

        return new FriendRequestListResponseDTO(sentDtos.size(), receivedDtos.size(), sentDtos, receivedDtos);
    }

    public FriendDetailResponseDTO getFriendDetail(Long currentUserId, Long friendId) {
        if (currentUserId.equals(friendId)) {
            throw new FriendHandler(ErrorStatus.INVALID_FRIEND_REQUEST);
        }

        boolean isFriend = friendRepository.existsFriendRelation(currentUserId, friendId);

        Member friend = memberRepository.findById(friendId)
                .orElseThrow(() -> new FriendHandler(ErrorStatus.MEMBER_NOT_FOUND));

        List<Diary> diaries = diaryRepository.findByMemberIdAndVisibilityForViewer(friendId, isFriend);

        List<MyDiarySummaryResponseDTO> diaryDtos = diaries.stream()
                .map(d -> MyDiarySummaryResponseDTO.builder()
                        .diaryId(d.getId())
                        .createdAt(d.getCreatedAt().toString())
                        .title(d.getTitle())
                        .visibility(d.getVisibility())
                        .thumbnailUrl(d.getThumbImg())
                        .likeCount(d.getLikeCount())
                        .commentCount(d.getCommentCount())
                        .correctionCount(d.getCorrectionCount())
                        .contentPreview(generateContentPreview(d.getContent()))
                        .language(d.getLanguage())
                        .build())
                .toList();

        return new FriendDetailResponseDTO(friend.getUsername(), friend.getNickname(), diaryDtos);
    }
}
