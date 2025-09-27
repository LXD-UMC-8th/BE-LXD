package org.lxdproject.lxd.diarycomment.service;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.code.exception.handler.CommentHandler;
import org.lxdproject.lxd.apiPayload.code.exception.handler.DiaryHandler;
import org.lxdproject.lxd.apiPayload.code.exception.handler.MemberHandler;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.authz.guard.PermissionGuard;
import org.lxdproject.lxd.common.dto.PageDTO;
import org.lxdproject.lxd.common.util.DateFormatUtil;
import org.lxdproject.lxd.config.security.SecurityUtil;
import org.lxdproject.lxd.diary.entity.Diary;
import org.lxdproject.lxd.diary.repository.DiaryRepository;
import org.lxdproject.lxd.diarycomment.dto.DiaryCommentDeleteResponseDTO;
import org.lxdproject.lxd.diarycomment.dto.DiaryCommentRequestDTO;
import org.lxdproject.lxd.diarycomment.dto.DiaryCommentResponseDTO;
import org.lxdproject.lxd.diarycomment.entity.DiaryComment;
import org.lxdproject.lxd.diarycomment.repository.DiaryCommentRepository;
import org.lxdproject.lxd.diarycommentlike.repository.DiaryCommentLikeRepository;
import org.lxdproject.lxd.member.entity.Member;
import org.lxdproject.lxd.member.repository.MemberRepository;
import org.lxdproject.lxd.notification.dto.NotificationRequestDTO;
import org.lxdproject.lxd.notification.entity.Notification;
import org.lxdproject.lxd.notification.entity.enums.NotificationType;
import org.lxdproject.lxd.notification.entity.enums.TargetType;
import org.lxdproject.lxd.notification.event.NotificationCreatedEvent;
import org.lxdproject.lxd.notification.service.NotificationService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.lxdproject.lxd.friend.repository.FriendRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class DiaryCommentService {

    private final DiaryCommentRepository diaryCommentRepository;
    private final MemberRepository memberRepository;
    private final DiaryRepository diaryRepository;
    private final DiaryCommentLikeRepository likeRepository;
    private final FriendRepository friendRepository;
    private final NotificationService notificationService;
    private final ApplicationEventPublisher eventPublisher;
    private final PermissionGuard permissionGuard;

    @Transactional
    public DiaryCommentResponseDTO writeComment(Long diaryId, DiaryCommentRequestDTO request) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        Member commentOwner = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));
        Diary diary = diaryRepository.findByIdAndDeletedAtIsNull(diaryId)
                .orElseThrow(() -> new DiaryHandler(ErrorStatus.DIARY_NOT_FOUND));
        Member diaryOwner = diary.getMember();

        // 친구 관계 여부 조회
        boolean areFriends = friendRepository.areFriends(memberId, diaryOwner.getId());

        // 댓글 작성 권한 검증
        permissionGuard.canCreateDiaryComment(memberId, diary, areFriends);

        DiaryComment parent = null;
        if (request.getParentId() != null) {
            parent = diaryCommentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new CommentHandler(ErrorStatus.PARENT_COMMENT_NOT_FOUND));

            // depth를 1로만 제한
            if (parent.getParent() != null) {
                throw new CommentHandler(ErrorStatus.COMMENT_DEPTH_EXCEEDED);
            }

            // 삭제된 부모 댓글에는 대댓글 제한
            if (parent.getDeletedAt() != null) {
                throw new CommentHandler(ErrorStatus.COMMENT_NOT_FOUND);
            }

            parent.increaseReplyCount();
        }


        DiaryComment comment = DiaryComment.builder()
                .member(commentOwner)
                .diary(diary)
                .parent(parent)
                .commentText(request.getCommentText())
                .likeCount(0)
                .build();

        DiaryComment saved = diaryCommentRepository.save(comment);
        diary.increaseCommentCount();

        // 알림 전송 (댓글 작성자 != 일기 작성자)
        if (!commentOwner.equals(diaryOwner)) {
            NotificationRequestDTO dto = NotificationRequestDTO.builder()
                    .receiverId(diaryOwner.getId())
                    .notificationType(NotificationType.COMMENT_ADDED)
                    .targetType(TargetType.DIARY_COMMENT)
                    .targetId(saved.getId())
                    .redirectUrl("/diaries/" + diary.getId() + "/comments/" + saved.getId())
                    .build();

            notificationService.createAndPublish(dto);
        }

        return DiaryCommentResponseDTO.builder()
                .commentId(saved.getId())
                .memberId(commentOwner.getId())
                .username(commentOwner.getUsername())
                .nickname(commentOwner.getNickname())
                .diaryId(diary.getId())
                .profileImage(commentOwner.getProfileImg())
                .commentText(saved.getCommentText())
                .parentId(parent != null ? parent.getId() : null)
                .replyCount(0)
                .likeCount(saved.getLikeCount())
                .isLiked(false)
                .createdAt(DateFormatUtil.formatDate(saved.getCreatedAt()))
                .build();
    }

    @Transactional(readOnly = true)
    public PageDTO<DiaryCommentResponseDTO.Comment> getComments(Long diaryId, int page, int size) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        int offset = page * size;

        // 부모 댓글
        List<DiaryComment> parents = diaryCommentRepository.findParentComments(diaryId, offset, size);

        List<Long> parentIds = parents.stream()
                .map(DiaryComment::getId)
                .toList();

        // 자식 댓글
        List<DiaryComment> replies = parentIds.isEmpty() ? List.of()
                : diaryCommentRepository.findRepliesByParentIds(parentIds);

        // 전체 댓글
        List<Long> allCommentIds = Stream.concat(parents.stream(), replies.stream())
                .map(DiaryComment::getId)
                .toList();

        // 좋아요 여부 조회
        Set<Long> likedCommentIds = allCommentIds.isEmpty() ? Set.of()
                : new HashSet<>(likeRepository.findLikedCommentIds(memberId, allCommentIds));

        Map<Long, List<DiaryComment>> repliesGroupedByParent = replies.stream()
                .collect(Collectors.groupingBy(reply -> reply.getParent().getId()));

                List<DiaryCommentResponseDTO.Comment> commentDTOs = parents.stream()
                .map(parent -> {
                    List<DiaryCommentResponseDTO.Comment> replyDTOs =
                            repliesGroupedByParent.getOrDefault(parent.getId(), List.of()).stream()
                                    .map(reply -> DiaryCommentResponseDTO.Comment.builder()
                                            .commentId(reply.getId())
                                            .parentId(parent.getId())
                                            .memberId(reply.getMember().getId())
                                            .username(reply.getMember().getUsername())
                                            .nickname(reply.getMember().getNickname())
                                            .profileImage(reply.getMember().getProfileImg())
                                            .content(reply.getCommentText())
                                            .likeCount(reply.getLikeCount())
                                            .isLiked(likedCommentIds.contains(reply.getId()))
                                            .createdAt(DateFormatUtil.formatDate(reply.getCreatedAt()))
                                            .replyCount(0)
                                            .replies(List.of())
                                            .build()
                                    ).toList();

                    return DiaryCommentResponseDTO.Comment.builder()
                            .commentId(parent.getId())
                            .parentId(null)
                            .memberId(parent.getMember().getId())
                            .username(parent.getMember().getUsername())
                            .nickname(parent.getMember().getNickname())
                            .profileImage(parent.getMember().getProfileImg())
                            .content(parent.getCommentText())
                            .likeCount(parent.getLikeCount())
                            .isLiked(likedCommentIds.contains(parent.getId()))
                            .createdAt(DateFormatUtil.formatDate(parent.getCreatedAt()))
                            .replyCount(parent.getReplyCount())
                            .replies(replyDTOs)
                            .build();
                })
                .toList();

        // 전체 댓글 개수(부모 + 자식)
        int totalElements = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new DiaryHandler(ErrorStatus.DIARY_NOT_FOUND))
                .getCommentCount();

        // hasNext는 부모 댓글 수 기준
        boolean hasNext = diaryCommentRepository.countParentComments(diaryId) > offset + size;

        return new PageDTO<>(
                (long) totalElements,
                commentDTOs,
                page + 1,
                size,
                hasNext
        );
    }

    @Transactional
    public DiaryCommentDeleteResponseDTO deleteComment(Long diaryId, Long commentId) {
        Diary diary = diaryRepository.findByIdAndDeletedAtIsNull(diaryId)
                .orElseThrow(() -> new DiaryHandler(ErrorStatus.DIARY_NOT_FOUND));

        DiaryComment comment = diaryCommentRepository.findById(commentId)
                .orElseThrow(() -> new CommentHandler(ErrorStatus.COMMENT_NOT_FOUND));

        comment.softDelete();
        diary.decreaseCommentCount();


        return DiaryCommentDeleteResponseDTO.from(comment);
    }

}
