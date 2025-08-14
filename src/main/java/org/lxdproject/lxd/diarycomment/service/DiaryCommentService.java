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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.lxdproject.lxd.member.repository.FriendRepository;

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

    @Transactional
    public DiaryCommentResponseDTO writeComment(Long memberId, Long diaryId, DiaryCommentRequestDTO request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));
        Diary diary = diaryRepository.findByIdAndDeletedAtIsNull(diaryId)
                .orElseThrow(() -> new DiaryHandler(ErrorStatus.DIARY_NOT_FOUND));

        Member diaryOwner = diary.getMember();
        CommentPermission permission = diary.getCommentPermission();

        //권한 확인

        if (permission == CommentPermission.NONE && !member.equals(diaryOwner)) {
            throw new CommentHandler(ErrorStatus.COMMENT_PERMISSION_DENIED);
        }

        if (permission == CommentPermission.FRIENDS && !member.equals(diaryOwner)) {
            if (!friendRepository.existsFriendRelation(diaryOwner.getId(), member.getId())) {
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
                .member(member)
                .diary(diary)
                .parent(parent)
                .commentText(request.getCommentText())
                .likeCount(0)
                .build();

        DiaryComment saved = diaryCommentRepository.save(comment);
        diary.increaseCommentCount();

        return DiaryCommentResponseDTO.builder()
                .commentId(saved.getId())
                .memberId(member.getId())
                .username(member.getUsername())
                .nickname(member.getNickname())
                .diaryId(diary.getId())
                .profileImage(member.getProfileImg())
                .commentText(saved.getCommentText())
                .parentId(parent != null ? parent.getId() : null)
                .replyCount(0)
                .likeCount(saved.getLikeCount())
                .isLiked(false)
                .createdAt(DateFormatUtil.formatDate(saved.getCreatedAt()))
                .build();
    }


    public PageResponse<DiaryCommentResponseDTO.Comment> getComments(Long diaryId, Pageable pageable) {
        Long memberId = SecurityUtil.getCurrentMemberId();

        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new DiaryHandler(ErrorStatus.DIARY_NOT_FOUND));
        int totalElements = diary.getCommentCount();

        Page<DiaryComment> parentPage =
                diaryCommentRepository.findByDiaryIdAndParentIsNull(diaryId, pageable);
        List<DiaryComment> parents = parentPage.getContent();
        List<Long> parentIds = parents.stream().map(DiaryComment::getId).toList();

        List<DiaryComment> replies = parentIds.isEmpty()
                ? List.of()
                : diaryCommentRepository.findByParentIdIn(parentIds);

        List<Long> allCommentIds = Stream.concat(parents.stream(), replies.stream())
                .map(DiaryComment::getId)
                .toList();

        Set<Long> likedCommentIds = allCommentIds.isEmpty()
                ? Set.of()
                : new HashSet<>(likeRepository.findLikedCommentIds(memberId, allCommentIds));

        Map<Long, List<DiaryComment>> repliesByParent = replies.stream()
                .collect(Collectors.groupingBy(c -> c.getParent().getId()));

        List<DiaryCommentResponseDTO.Comment> dtoTree =
                DiaryCommentConverter.toCommentDtoTree(parents, repliesByParent, likedCommentIds);


        return new org.lxdproject.lxd.common.dto.PageResponse<
                org.lxdproject.lxd.diarycomment.dto.DiaryCommentResponseDTO.Comment>(
                (long) totalElements,
                dtoTree,
                parentPage.getNumber(),
                parentPage.getSize(),
                parentPage.getTotalPages(),
                parentPage.hasNext()
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
