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

@Service
@RequiredArgsConstructor
public class CorrectionCommentService {

    private final CorrectionCommentRepository commentRepository;
    private final CorrectionRepository correctionRepository;
    private final MemberRepository memberRepository;
    //like구현필요 likeRepository

    public CorrectionCommentResponseDTO createComment(Long correctionId, CorrectionCommentRequestDTO request, Long memberId) {
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
                .commentText(request.getCommentText())
                .likeCount(0)
                .build();

        CorrectionComment saved = commentRepository.save(comment);

        return CorrectionCommentResponseDTO.builder()
                .commentId(saved.getId())
                .nickname(member.getNickname())
                .profileImage(member.getProfileImg())
                .content(saved.getCommentText())
                .likeCount(saved.getLikeCount())
                .isLiked(false)
                .createdAt(saved.getCreatedAt())
                .build();
    }

    public CorrectionCommentPageResponseDTO getComments(Long correctionId, Long userId, Pageable pageable) {
        Page<CorrectionComment> comments = commentRepository.findByCorrectionIdWithOldestFirst(correctionId, pageable);

        List<CorrectionCommentResponseDTO> content = comments.getContent().stream()
                .map(comment -> CorrectionCommentResponseDTO.builder()
                        .commentId(comment.getId())
                        .nickname(comment.getMember().getNickname())
                        .profileImage(comment.getMember().getProfileImg())
                        .content(comment.getCommentText())
                        .likeCount(comment.getLikeCount())
                        .isLiked(false) // 좋아요 여부는 일단 false 처리
                        .createdAt(comment.getCreatedAt())
                        .build()
                )
                .toList();

        return CorrectionCommentPageResponseDTO.builder()
                .replies(content)
                .totalElements(comments.getTotalElements())
                .build();
    }

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

