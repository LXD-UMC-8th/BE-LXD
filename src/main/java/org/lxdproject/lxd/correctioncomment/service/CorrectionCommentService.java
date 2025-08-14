package org.lxdproject.lxd.correctioncomment.service;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.code.exception.handler.AuthHandler;
import org.lxdproject.lxd.apiPayload.code.exception.handler.CommentHandler;
import org.lxdproject.lxd.apiPayload.code.exception.handler.CorrectionHandler;
import org.lxdproject.lxd.apiPayload.code.exception.handler.MemberHandler;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.common.dto.PageResponse;
import org.lxdproject.lxd.common.util.DateFormatUtil;
import org.lxdproject.lxd.config.security.SecurityUtil;
import org.lxdproject.lxd.correction.entity.Correction;
import org.lxdproject.lxd.correction.repository.CorrectionRepository;
import org.lxdproject.lxd.correctioncomment.dto.*;
import org.lxdproject.lxd.correctioncomment.entity.CorrectionComment;
import org.lxdproject.lxd.correctioncomment.repository.CorrectionCommentRepository;
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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CorrectionCommentService {

    private final CorrectionCommentRepository commentRepository;
    private final CorrectionRepository correctionRepository;
    private final MemberRepository memberRepository;

    private final NotificationService notificationService;

    @Transactional
    public CorrectionCommentResponseDTO writeComment(Long correctionId, CorrectionCommentRequestDTO request) {

        Member member = memberRepository.findById(SecurityUtil.getCurrentMemberId())
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));
        Correction correction = correctionRepository.findById(correctionId)
                .orElseThrow(() -> new CorrectionHandler(ErrorStatus.CORRECTION_NOT_FOUND));

        CorrectionComment comment = CorrectionComment.builder()
                .member(member)
                .correction(correction)
                .content(request.getContent())
                .build();

        CorrectionComment saved = commentRepository.save(comment);

        // 알림 전송 (댓글 작성자 != 교정 작성자)
        Member correctionAuthor = correction.getAuthor();
        if (!member.equals(correctionAuthor)) {
            NotificationRequestDTO dto = NotificationRequestDTO.builder()
                    .receiverId(correctionAuthor.getId())
                    .notificationType(NotificationType.CORRECTION_REPLIED)
                    .targetType(TargetType.CORRECTION_COMMENT)
                    .targetId(saved.getId())
                    .redirectUrl("/diaries/" + correction.getDiary().getId() + "/corrections/" + correction.getId() + "/comments/" + saved.getId())
                    .build();

            notificationService.saveAndPublishNotification(dto);
        }

        return CorrectionCommentResponseDTO.builder()
                .commentId(saved.getId())
                .memberId(member.getId())
                .username(member.getUsername())
                .nickname(member.getNickname())
                .profileImage(member.getProfileImg())
                .content(saved.getContent())
                .createdAt(DateFormatUtil.formatDate(saved.getCreatedAt()))
                .build();
    }


    @Transactional(readOnly = true)
    public PageResponse<CorrectionCommentResponseDTO> getComments(Long correctionId, Pageable pageable) {
        Correction correction = correctionRepository.findById(correctionId)
                .orElseThrow(() -> new CorrectionHandler(ErrorStatus.CORRECTION_NOT_FOUND));

        Page<CorrectionComment> commentPage = commentRepository.findByCorrection(correction, pageable);


        List<CorrectionCommentResponseDTO> content = commentPage.stream()
                .map(comment -> CorrectionCommentResponseDTO.builder()
                        .commentId(comment.getId())
                        .memberId(comment.getMember().getId())
                        .username(comment.getMember().getUsername())
                        .nickname(comment.getMember().getNickname())
                        .profileImage(comment.getMember().getProfileImg())
                        .content(comment.getDisplayContent())
                        .createdAt(DateFormatUtil.formatDate(comment.getCreatedAt()))
                        .build())
                .toList();

        return new PageResponse<>(
                commentPage.getTotalElements(),
                content,
                commentPage.getNumber()+1,
                commentPage.getSize(),
                commentPage.hasNext()
        );
    }

    //삭제
    @Transactional
    public CorrectionCommentDeleteResponseDTO deleteComment(Long commentId) {
        CorrectionComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentHandler(ErrorStatus.COMMENT_NOT_FOUND));

        if (!comment.getMember().getId().equals(SecurityUtil.getCurrentMemberId())) {
            throw new AuthHandler(ErrorStatus.NOT_RESOURCE_OWNER);
        }

        comment.softDelete();

        return CorrectionCommentDeleteResponseDTO.builder()
                .commentId(comment.getId())
                .isDeleted(comment.isDeleted())
                .content(comment.getDisplayContent())
                .deletedAt(comment.getDeletedAt())
                .build();
    }
}
