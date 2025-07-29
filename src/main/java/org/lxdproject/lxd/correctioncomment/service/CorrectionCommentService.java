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
        Page<CorrectionComment> comments = CorrectionCommentRepository.findByCorrectionIdWithOldestFirst(correctionId, pageable);

        List<CorrectionCommentResponseDTO> content = comments.getContent().stream()
                .map(comment -> CorrectionCommentResponseDTO.builder()
                        .commentId(comment.getId())
                        .nickname(comment.getMember().getNickname())
                        .profileImage(comment.getMember().getProfileImage())
                        .content(comment.getCommentText())
                        .likeCount(comment.getLikes().size())
                        .isLiked(false) // 좋아요 여부는 일단 false 처리 (추후 로직 필요)
                        .createdAt(comment.getCreatedAt())
                        .build()
                )
                .toList();

        return CorrectionCommentPageResponseDTO.builder()
                .content(content)
                .totalElements(comments.getTotalElements())
                .build();
    }


    public CorrectionCommentPageResponseDTO getComments(Long correctionId, Long memberId, Pageable pageable) {
        Page<CorrectionComment> comments = CorrectionCommentRepository.findByCorrectionIdWithOldestFirst(correctionId, pageable);

        List<CorrectionCommentResponseDTO> content = comments.getContent().stream()
                .map(comment -> CorrectionCommentResponseDTO.builder()
                        .commentId(comment.getId())
                        .parentId(null)
                        .nickname(comment.getMember().getNickname())
                        .profileImage(comment.getMember().getProfileImage())
                        .content(comment.getCommentText()) // 삭제된 댓글 처리 포함
                        .likeCount(comment.getLikes().size())
                        .isLiked(comment.getLikes().stream()
                                .anyMatch(like -> like.getMember().getId().equals(memberId)))
                        .createdAt(comment.getCreatedAt())
                        .build())
                .toList();

        return CorrectionCommentPageResponseDTO.builder()
                .content(content)
                .totalElements(comments.getTotalElements())
                .build();
    }



}

