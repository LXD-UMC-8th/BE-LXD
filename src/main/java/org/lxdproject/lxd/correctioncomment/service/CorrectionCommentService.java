package org.lxdproject.lxd.correctioncomment.service;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.code.exception.handler.CommentHandler;
import org.lxdproject.lxd.apiPayload.code.exception.handler.CorrectionHandler;
import org.lxdproject.lxd.apiPayload.code.exception.handler.MemberHandler;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.correction.entity.Correction;
import org.lxdproject.lxd.correction.repository.CorrectionRepository;
import org.lxdproject.lxd.correctioncomment.dto.*;
import org.lxdproject.lxd.correctioncomment.entity.CorrectionComment;
import org.lxdproject.lxd.correctioncomment.repository.CorrectionCommentRepository;
import org.lxdproject.lxd.member.entity.Member;
import org.lxdproject.lxd.member.repository.MemberRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CorrectionCommentService {

    private final CorrectionCommentRepository commentRepository;
    private final CorrectionRepository correctionRepository;
    private final MemberRepository memberRepository;


    public CorrectionCommentResponseDTO writeComment(Long memberId, Long correctionId, CorrectionCommentRequestDTO request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));
        Correction correction = correctionRepository.findById(correctionId)
                .orElseThrow(() -> new CorrectionHandler(ErrorStatus.CORRECTION_NOT_FOUND));

        CorrectionComment parent = null;
        if (request.getParentId() != null) {
            parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new CommentHandler(ErrorStatus.PARENT_COMMENT_NOT_FOUND));

            if (parent.getParent() != null) {
                throw new CommentHandler(ErrorStatus.COMMENT_DEPTH_EXCEEDED);
            }
        }

        CorrectionComment comment = CorrectionComment.builder()
                .member(member)
                .correction(correction)
                .parent(parent)
                .commentText(request.getCommentText())
                .likeCount(0)
                .build();

        CorrectionComment saved = commentRepository.save(comment);

        return CorrectionCommentResponseDTO.builder()
                .commentId(saved.getId())
                .nickname(member.getNickname())
                .profileImage(member.getProfileImg())
                .content(saved.getCommentText())
                .parentId(parent != null ? parent.getId() : null)
                .likeCount(saved.getLikeCount())
                .isLiked(false)
                .createdAt(saved.getCreatedAt())
                .build();
    }


    //조회
    @Transactional(readOnly = true)
    public CorrectionCommentPageResponseDTO getComments(Long correctionId, Long memberId, Pageable pageable) {
        // 부모 댓글 조회
        Page<CorrectionComment> parentComments = commentRepository.findByCorrectionIdAndParentIsNull(correctionId, pageable);
        List<CorrectionComment> parents = parentComments.getContent();

        // 부모 ID 목록
        List<Long> parentIds = parents.stream()
                .map(CorrectionComment::getId)
                .toList();

        // 대댓글 일괄 조회
        List<CorrectionComment> childComments = parentIds.isEmpty() ? List.of() :
                commentRepository.findByParentIdIn(parentIds);

        // 대댓글을 parentId 기준으로 그룹핑
        Map<Long, List<CorrectionComment>> groupedReplies = childComments.stream()
                //.filter(c -> !c.isDeleted())   대댓글은 그냥 삭제시키고 싶으면 이 코드 사용
                .collect(Collectors.groupingBy(c -> c.getParent().getId()));

        // DTO 트리 구조 생성
        List<CorrectionCommentPageResponseDTO.Comment> result = parents.stream().map(parent -> {
            // 대댓글 DTO 목록
            List<CorrectionCommentPageResponseDTO.Comment> replyDtos = groupedReplies.getOrDefault(parent.getId(), List.of())
                    .stream().map(child ->
                            CorrectionCommentPageResponseDTO.Comment.builder()
                                    .commentId(child.getId())
                                    .parentId(parent.getId())
                                    .userId(child.getMember().getId())
                                    .nickname(child.getMember().getNickname())
                                    .profileImage(child.getMember().getProfileImg())
                                    .content(child.getCommentText())
                                    .likeCount(child.getLikeCount())
                                    .isLiked(false)
                                    .createdAt(child.getCreatedAt())
                                    .replies(List.of()) // 대대댓글 방지
                                    .build()
                    ).toList();

            // 부모 댓글에 replies 세팅
            return CorrectionCommentPageResponseDTO.Comment.builder()
                    .commentId(parent.getId())
                    .parentId(null)
                    .userId(parent.getMember().getId())
                    .nickname(parent.getMember().getNickname())
                    .profileImage(parent.getMember().getProfileImg())
                    .content(parent.getCommentText())
                    .likeCount(parent.getLikeCount())
                    .isLiked(false)
                    .createdAt(parent.getCreatedAt())
                    .replies(replyDtos)
                    .build();
        }).toList();

        long totalElements = parents.size() + childComments.size();

        return CorrectionCommentPageResponseDTO.builder()
                .replies(result)
                .totalElements(totalElements)
                .build();
    }


    //삭제
    @Transactional
    public CorrectionCommentDeleteResponseDTO deleteComment(Long commentId, Long userId) {
        CorrectionComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentHandler(ErrorStatus.COMMENT_NOT_FOUND));

        if (!comment.getMember().getId().equals(userId)) {
            throw new CommentHandler(ErrorStatus.COMMENT_NOT_FOUND); // 자신이 쓴 댓글만 삭제 가능
        }

        comment.softDelete(); // 삭제 시간 세팅
        return CorrectionCommentDeleteResponseDTO.builder()
                .commentId(comment.getId())
                .isDeleted(comment.isDeleted())
                .content(comment.getCommentText())  // "삭제된 댓글입니다."
                .deletedAt(comment.getDeletedAt())
                .build();
    }
}
