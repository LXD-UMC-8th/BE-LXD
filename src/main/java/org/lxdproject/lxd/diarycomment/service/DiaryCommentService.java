package org.lxdproject.lxd.diarycomment.service;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.code.exception.handler.CommentHandler;
import org.lxdproject.lxd.apiPayload.code.exception.handler.DiaryHandler;
import org.lxdproject.lxd.apiPayload.code.exception.handler.MemberHandler;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DiaryCommentService {

    private final DiaryCommentRepository diaryCommentRepository;
    private final MemberRepository memberRepository;
    private final DiaryRepository diaryRepository;
    private final DiaryCommentLikeRepository likeRepository;

    public DiaryCommentResponseDTO writeComment(Long memberId, Long diaryId, DiaryCommentRequestDTO request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));
        Diary diary = diaryRepository.findByIdAndDeletedAtIsNull(diaryId)
                .orElseThrow(() -> new DiaryHandler(ErrorStatus.DIARY_NOT_FOUND));

        DiaryComment parent = null;
        if (request.getParentId() != null) {
            parent = diaryCommentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new CommentHandler(ErrorStatus.PARENT_COMMENT_NOT_FOUND));
        }

        DiaryComment comment = DiaryComment.builder()
                .member(member)
                .diary(diary)
                .parent(parent)
                .commentText(request.getCommentText())
                .likeCount(0)
                .build();

        DiaryComment saved = diaryCommentRepository.save(comment);

        return DiaryCommentResponseDTO.builder()
                .commentId(saved.getId())
                .userId(member.getId())
                .nickname(member.getNickname())
                .diaryId(diary.getId())
                .profileImage(member.getProfileImg())
                .commentText(saved.getCommentText())
                .parentId(parent != null ? parent.getId() : null)
                .createdAt(saved.getCreatedAt())
                .build();
    }

    public DiaryCommentResponseDTO.CommentList getComments(Long diaryId, Pageable pageable) {
        Page<DiaryComment> parentComments = diaryCommentRepository.findByDiaryIdAndParentIsNull(diaryId, pageable);
        Long memberId = SecurityUtil.getCurrentMemberId();

        List<DiaryCommentResponseDTO.Comment> commentDTOs = parentComments.getContent().stream().map(parent -> {
            // 대댓글 가져오기
            List<DiaryComment> replies = diaryCommentRepository.findByParent(parent);

            List<DiaryCommentResponseDTO.Comment> replyDTOs = replies.stream().map(reply -> {
                return DiaryCommentResponseDTO.Comment.builder()
                        .commentId(reply.getId())
                        .parentId(parent.getId())
                        .userId(reply.getMember().getId())
                        .nickname(reply.getMember().getNickname())
                        .profileImage(reply.getMember().getProfileImg())
                        .content(reply.getCommentText())
                        .likeCount(reply.getLikeCount())
                        .isLiked(likeRepository.existsByCommentAndMemberId(reply, memberId))
                        .createdAt(reply.getCreatedAt())
                        .replies(List.of())
                        .build();
            }).toList();

            return DiaryCommentResponseDTO.Comment.builder()
                    .commentId(parent.getId())
                    .parentId(null)
                    .userId(parent.getMember().getId())
                    .nickname(parent.getMember().getNickname())
                    .profileImage(parent.getMember().getProfileImg())
                    .content(parent.getCommentText())
                    .likeCount(parent.getLikeCount())
                    .isLiked(likeRepository.existsByCommentAndMemberId(parent, memberId))
                    .createdAt(parent.getCreatedAt())
                    .replies(replyDTOs)
                    .build();
        }).toList();

        return DiaryCommentResponseDTO.CommentList.builder()
                .content(commentDTOs)
                .totalElements((int) parentComments.getTotalElements())
                .build();
    }


    //댓글삭제
    public DiaryCommentDeleteResponseDTO deleteComment(Long diaryId, Long commentId) {
        DiaryComment comment = diaryCommentRepository.findById(commentId)
                .orElseThrow(() -> new CommentHandler(ErrorStatus.COMMENT_NOT_FOUND));

        comment.softDelete();

        // Todo diary.decreaseCommentCount();

        return DiaryCommentDeleteResponseDTO.from(comment); // DTO로 반환
    }

}
