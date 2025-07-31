package org.lxdproject.lxd.diarycomment.service;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.code.exception.handler.CommentHandler;
import org.lxdproject.lxd.apiPayload.code.exception.handler.DiaryHandler;
import org.lxdproject.lxd.apiPayload.code.exception.handler.MemberHandler;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.config.security.SecurityUtil;
import org.lxdproject.lxd.diary.entity.Diary;
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

    @Transactional
    public DiaryCommentResponseDTO writeComment(Long memberId, Long diaryId, DiaryCommentRequestDTO request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));
        Diary diary = diaryRepository.findByIdAndDeletedAtIsNull(diaryId)
                .orElseThrow(() -> new DiaryHandler(ErrorStatus.DIARY_NOT_FOUND));

        DiaryComment parent = null;
        if (request.getParentId() != null) {
            parent = diaryCommentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new CommentHandler(ErrorStatus.PARENT_COMMENT_NOT_FOUND));

            if (parent.getParent() != null) {
                throw new CommentHandler(ErrorStatus.COMMENT_DEPTH_EXCEEDED);
            }
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
                .userId(member.getId())
                .nickname(member.getNickname())
                .diaryId(diary.getId())
                .profileImage(member.getProfileImg())
                .commentText(saved.getCommentText())
                .parentId(parent != null ? parent.getId() : null)
                .likeCount(saved.getLikeCount())
                .isLiked(false)
                .createdAt(saved.getCreatedAt())
                .build();
    }

    public DiaryCommentResponseDTO.CommentList getComments(Long diaryId, Pageable pageable) {
        Long memberId = SecurityUtil.getCurrentMemberId();

        // 부모 댓글 조회
        Page<DiaryComment> parentComments = diaryCommentRepository.findByDiaryIdAndParentIsNull(diaryId, pageable);
        List<DiaryComment> parents = parentComments.getContent();
        List<Long> parentIds = parents.stream().map(DiaryComment::getId).toList();

        // 자식 댓글 일괄 조회
        List<DiaryComment> allReplies = parentIds.isEmpty() ? List.of() :
                diaryCommentRepository.findByParentIdIn(parentIds);

        // 부모 + 자식 댓글 ID 리스트에 담기
        List<Long> allCommentIds = Stream.concat(parents.stream(), allReplies.stream())
                .map(DiaryComment::getId)
                .toList();

        // 부모 + 자식 댓글 ID의 좋아요 여부 일괄 조회
        Set<Long> likedCommentIds = allCommentIds.isEmpty() ? Set.of()
                : new HashSet<>(likeRepository.findLikedCommentIds(memberId, allCommentIds));

        // 부모 + 자식 그룹핑
        Map<Long, List<DiaryComment>> repliesGroupedByParent = allReplies.stream()
                .collect(Collectors.groupingBy(reply -> reply.getParent().getId()));

        // DTO 변환
        List<DiaryCommentResponseDTO.Comment> commentDTOs =
                DiaryCommentConverter.toCommentDtoTree(parents, repliesGroupedByParent, likedCommentIds);

        int totalElements = parents.size() + allReplies.size();

        return DiaryCommentResponseDTO.CommentList.builder()
                .content(commentDTOs)
                .totalElements(totalElements)
                .build();
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
