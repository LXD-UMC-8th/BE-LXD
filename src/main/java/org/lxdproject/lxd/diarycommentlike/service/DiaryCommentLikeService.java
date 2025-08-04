package org.lxdproject.lxd.diarycommentlike.service;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.code.exception.handler.CommentHandler;
import org.lxdproject.lxd.apiPayload.code.exception.handler.MemberHandler;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.config.security.SecurityUtil;
import org.lxdproject.lxd.diarycomment.entity.DiaryComment;
import org.lxdproject.lxd.diarycomment.repository.DiaryCommentRepository;
import org.lxdproject.lxd.diarycommentlike.dto.DiaryCommentLikeResponseDTO;
import org.lxdproject.lxd.diarycommentlike.entity.DiaryCommentLike;
import org.lxdproject.lxd.diarycommentlike.repository.DiaryCommentLikeRepository;
import org.lxdproject.lxd.member.entity.Member;
import org.lxdproject.lxd.member.repository.MemberRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DiaryCommentLikeService {

    private final MemberRepository memberRepository;
    private final DiaryCommentRepository commentRepository;
    private final DiaryCommentLikeRepository likeRepository;

    public DiaryCommentLikeResponseDTO toggleLike(Long commentId) {
        Long memberId = SecurityUtil.getCurrentMemberId();

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        DiaryComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentHandler(ErrorStatus.COMMENT_NOT_FOUND));

        Optional<DiaryCommentLike> existing = likeRepository.findByMemberIdAndCommentId(memberId, commentId);

        boolean liked;

        if (existing.isPresent()) {
            likeRepository.delete(existing.get());;
            comment.decreaseLikeCount();
            commentRepository.save(comment);
            liked = false;
        } else {
            likeRepository.save(
                    DiaryCommentLike.builder()
                            .member(member)
                            .comment(comment)
                            .build()
            );
            comment.increaseLikeCount();
            commentRepository.save(comment);
            liked = true;
        }

        return DiaryCommentLikeResponseDTO.builder()
                .commentId(commentId)
                .memberId(memberId)
                .liked(liked)
                .likeCount(comment.getLikeCount())
                .build();
    }
}



