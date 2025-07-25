package org.lxdproject.lxd.correctioncomment.service;

import lombok.RequiredArgsConstructor;
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

import java.util.List;

@Service
@RequiredArgsConstructor
public class CorrectionCommentService {

    private final CorrectionCommentRepository commentRepository;
    private final CorrectionRepository correctionRepository;
    private final MemberRepository memberRepository;

    public CorrectionCommentResponseDTO createComment(Long correctionId, CorrectionCommentRequestDTO request, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));
        Correction correction = correctionRepository.findById(correctionId)
                .orElseThrow(() -> new IllegalArgumentException("교정 없음"));

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

    public CorrectionCommentPageResponseDTO getComments(Long correctionId, Pageable pageable) {
        Page<CorrectionComment> page = commentRepository.findAllByCorrectionId(correctionId, pageable);
        List<CorrectionCommentResponseDTO> comments = page.stream().map(comment -> CorrectionCommentResponseDTO.builder()
                .commentId(comment.getId())
                .nickname(comment.getMember().getNickname())
                .profileImage(comment.getMember().getProfileImg())
                .content(comment.getCommentText())
                .likeCount(comment.getLikeCount())
                .isLiked(false)
                .createdAt(comment.getCreatedAt())
                .build()).toList();

        return CorrectionCommentPageResponseDTO.builder()
                .comments(comments)
                .totalElements(page.getTotalElements())
                .build();
    }

    public CorrectionCommentDeleteResponseDTO deleteComment(Long correctionId, Long commentId, Long memberId) {
        CorrectionComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글 없음"));
        if (!comment.getCorrection().getId().equals(correctionId)) {
            throw new IllegalArgumentException("correction 불일치");
        }
        if (!comment.getMember().getId().equals(memberId)) {
            throw new IllegalArgumentException("작성자 불일치");
        }

        comment.softDelete(); // soft delete
        return CorrectionCommentDeleteResponseDTO.builder()
                .commentId(comment.getId())
                .isDeleted(true)
                .content(comment.getCommentText())
                .deletedAt(comment.getUpdatedAt())
                .build();

    }
}

