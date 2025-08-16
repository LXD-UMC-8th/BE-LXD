package org.lxdproject.lxd.diarycomment.service;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.code.exception.handler.CommentHandler;
import org.lxdproject.lxd.apiPayload.code.exception.handler.DiaryHandler;
import org.lxdproject.lxd.apiPayload.code.exception.handler.MemberHandler;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.common.dto.PageResponse;
import org.lxdproject.lxd.common.util.DateFormatUtil;
import org.lxdproject.lxd.config.security.SecurityUtil;
import org.lxdproject.lxd.diary.entity.Diary;
import org.lxdproject.lxd.diary.entity.enums.CommentPermission;
import org.lxdproject.lxd.diary.repository.DiaryRepository;
import org.lxdproject.lxd.diarycomment.converter.DiaryCommentConverter;
import org.lxdproject.lxd.diarycomment.dto.DiaryCommentDeleteResponseDTO;
import org.lxdproject.lxd.diarycomment.dto.DiaryCommentRequestDTO;
import org.lxdproject.lxd.diarycomment.dto.DiaryCommentResponseDTO;
import org.lxdproject.lxd.diarycomment.entity.DiaryComment;
import org.lxdproject.lxd.diarycomment.repository.DiaryCommentRepository;
import org.lxdproject.lxd.diarycommentlike.repository.DiaryCommentLikeRepository;
import org.lxdproject.lxd.member.entity.Member;
import org.lxdproject.lxd.member.repository.MemberRepository;
import org.lxdproject.lxd.notification.dto.NotificationRequestDTO;
import org.lxdproject.lxd.notification.entity.enums.NotificationType;
import org.lxdproject.lxd.notification.entity.enums.TargetType;
import org.lxdproject.lxd.notification.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Transactional
    public DiaryCommentResponseDTO writeComment(Long diaryId, DiaryCommentRequestDTO request) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        Member commentOwner = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));
        Diary diary = diaryRepository.findByIdAndDeletedAtIsNull(diaryId)
                .orElseThrow(() -> new DiaryHandler(ErrorStatus.DIARY_NOT_FOUND));

        Member diaryOwner = diary.getMember();
        CommentPermission permission = diary.getCommentPermission();

        //권한 확인

        if (permission == CommentPermission.NONE && !commentOwner.equals(diaryOwner)) {
            throw new CommentHandler(ErrorStatus.COMMENT_PERMISSION_DENIED);
        }

        if (permission == CommentPermission.FRIENDS && !commentOwner.equals(diaryOwner)) {
            if (!friendRepository.existsFriendRelation(diaryOwner.getId(), commentOwner.getId())) {
                throw new CommentHandler(ErrorStatus.COMMENT_PERMISSION_DENIED);
            }
        }

        DiaryComment parent = null;
        if (request.getParentId() != null) {
            parent = diaryCommentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new CommentHandler(ErrorStatus.PARENT_COMMENT_NOT_FOUND));

            if (parent.isDeleted()) {
                throw new CommentHandler(ErrorStatus.PARENT_COMMENT_NOT_FOUND);
            }
            if (parent.getParent() != null) {
                throw new CommentHandler(ErrorStatus.COMMENT_DEPTH_EXCEEDED);
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

            notificationService.saveAndPublishNotification(dto);
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


    public DiaryCommentResponseDTO.ExtendedPageResponse<DiaryCommentResponseDTO.Comment> getComments(Long diaryId, Pageable pageable) {
        Long memberId = SecurityUtil.getCurrentMemberId();

        // 부모 페이징(부모 기준)
        Page<DiaryComment> parentPage =
                diaryCommentRepository.findByDiaryIdAndParentIsNull(diaryId, pageable);
        List<DiaryComment> parents = parentPage.getContent();
        List<Long> parentIds = parents.stream().map(DiaryComment::getId).toList();

        // 대댓글 일괄 조회
        List<DiaryComment> replies = parentIds.isEmpty()
                ? List.of()
                : diaryCommentRepository.findByParentIdIn(parentIds);

        // 좋아요 여부(isLiked)용 조회
        List<Long> allCommentIds = Stream.concat(parents.stream(), replies.stream())
                .map(DiaryComment::getId)
                .toList();
        Set<Long> likedCommentIds = allCommentIds.isEmpty()
                ? Set.of()
                : new HashSet<>(likeRepository.findLikedCommentIds(memberId, allCommentIds));

        // 부모 → 대댓글 그룹핑
        Map<Long, List<DiaryComment>> repliesByParent = replies.stream()
                .collect(Collectors.groupingBy(c -> c.getParent().getId()));

        // DTO 변환
        List<DiaryCommentResponseDTO.Comment> dtoTree =
                DiaryCommentConverter.toCommentDtoTree(parents, repliesByParent, likedCommentIds);

        // 이 페이지에서 실제 내려간 아이템 수(부모+대댓글)
        int pageItemCount = dtoTree.size()
                + dtoTree.stream().mapToInt(c -> c.getReplies() == null ? 0 : c.getReplies().size()).sum();

        // 전체 개수(부모+대댓글 전체, 삭제 포함)
        long totalAll = diaryCommentRepository.countAllCommentsIncludingDeleted(diaryId);

        // 부모 총 개수/총 페이지(페이징 기준)
        long parentTotal = parentPage.getTotalElements();
        int totalPages = parentPage.getTotalPages();

        // 페이지/사이즈(0-based 유지; 1-based 원하면 +1 해서 넘겨도 됨)
        int page = parentPage.getNumber();
        int size = parentPage.getSize();

        return new DiaryCommentResponseDTO.ExtendedPageResponse<>(
                totalAll,                     // totalElements (부모+대댓글 전체)
                parentTotal,                  // parentTotalElements (부모 총 개수)
                page,                         // page
                size,                         // size
                totalPages,                   // totalPages (부모 기준)
                pageItemCount,                // pageItemCount (부모+대댓글)
                parentPage.hasNext(),         // hasNext
                dtoTree                       // contents
        );
    }


    @Transactional
    public DiaryCommentDeleteResponseDTO deleteComment(Long diaryId, Long commentId) {
        Diary diary = diaryRepository.findByIdAndDeletedAtIsNull(diaryId)
                .orElseThrow(() -> new DiaryHandler(ErrorStatus.DIARY_NOT_FOUND));

        DiaryComment comment = diaryCommentRepository.findById(commentId)
                .orElseThrow(() -> new CommentHandler(ErrorStatus.COMMENT_NOT_FOUND));

        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        if (!comment.getMember().getId().equals(currentMemberId)) {
            throw new CommentHandler(ErrorStatus.COMMENT_PERMISSION_DENIED);
        }

        comment.softDelete();

        return DiaryCommentDeleteResponseDTO.from(comment);
    }

}
