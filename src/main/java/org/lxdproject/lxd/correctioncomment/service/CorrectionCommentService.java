package org.lxdproject.lxd.correctioncomment.service;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.code.exception.handler.CommentHandler;
import org.lxdproject.lxd.apiPayload.code.exception.handler.CorrectionHandler;
import org.lxdproject.lxd.apiPayload.code.exception.handler.MemberHandler;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.config.security.SecurityUtil;
import org.lxdproject.lxd.correction.entity.Correction;
import org.lxdproject.lxd.correction.repository.CorrectionRepository;
import org.lxdproject.lxd.correctioncomment.dto.*;
import org.lxdproject.lxd.correctioncomment.entity.CorrectionComment;
import org.lxdproject.lxd.correctioncomment.repository.CorrectionCommentRepository;
import org.lxdproject.lxd.diarycomment.dto.DiaryCommentRequestDTO;
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


    //생성

    private final CorrectionCommentRepository commentRepository;
    private final CorrectionRepository correctionRepository;
    private final MemberRepository memberRepository;
    //like구현필요 likeRepository

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
        // 부모 댓글만 조회
        Page<CorrectionComment> parentComments = commentRepository.findByCorrectionIdAndParentIsNull(correctionId, pageable);

        List<Long> parentIds = parentComments.getContent().stream()
                .map(CorrectionComment::getId)
                .toList();

        List<CorrectionComment> childComments = commentRepository.findByParentIdIn(parentIds);

        Map<Long, List<CorrectionComment>> groupedChildren = childComments.stream()
                .filter(c -> !c.isDeleted())
                .collect(Collectors.groupingBy(c -> c.getParent().getId()));

        // 댓글 1개씩 DTO로 변환 (대댓글 없이)
        List<CorrectionCommentResponseDTO> flatCommentList = new java.util.ArrayList<>();

        for (CorrectionComment parent : parentComments.getContent()) {
            // 부모 댓글
            CorrectionCommentResponseDTO parentDTO = CorrectionCommentResponseDTO.builder()
                    .commentId(parent.getId())
                    .nickname(parent.getMember().getNickname())
                    .profileImage(parent.getMember().getProfileImg())
                    .content(parent.getCommentText())
                    .parentId(null)
                    .likeCount(parent.getLikeCount())
                    .isLiked(false)
                    .createdAt(parent.getCreatedAt())
                    .build();

            flatCommentList.add(parentDTO);

            // 자식 댓글들
            List<CorrectionComment> children = groupedChildren.getOrDefault(parent.getId(), List.of());
            for (CorrectionComment child : children) {
                CorrectionCommentResponseDTO childDTO = CorrectionCommentResponseDTO.builder()
                        .commentId(child.getId())
                        .nickname(child.getMember().getNickname())
                        .profileImage(child.getMember().getProfileImg())
                        .content(child.getCommentText())
                        .parentId(parent.getId())
                        .likeCount(child.getLikeCount())
                        .isLiked(false)
                        .createdAt(child.getCreatedAt())
                        .build();
                flatCommentList.add(childDTO);
            }
        }

        return CorrectionCommentPageResponseDTO.builder()
                .replies(flatCommentList)
                .totalElements(parentComments.getTotalElements())
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
