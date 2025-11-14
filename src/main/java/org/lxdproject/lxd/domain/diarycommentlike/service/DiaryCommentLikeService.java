package org.lxdproject.lxd.domain.diarycommentlike.service;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.global.apiPayload.code.exception.handler.CommentHandler;
import org.lxdproject.lxd.global.apiPayload.code.exception.handler.MemberHandler;
import org.lxdproject.lxd.global.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.global.authz.guard.MemberGuard;
import org.lxdproject.lxd.global.common.dto.MemberProfileDTO;
import org.lxdproject.lxd.global.security.SecurityUtil;
import org.lxdproject.lxd.domain.diarycomment.entity.DiaryComment;
import org.lxdproject.lxd.domain.diarycomment.repository.DiaryCommentRepository;
import org.lxdproject.lxd.domain.diarycommentlike.dto.DiaryCommentLikeResponseDTO;
import org.lxdproject.lxd.domain.diarycommentlike.entity.DiaryCommentLike;
import org.lxdproject.lxd.domain.diarycommentlike.repository.DiaryCommentLikeRepository;
import org.lxdproject.lxd.domain.member.entity.Member;
import org.lxdproject.lxd.domain.member.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class DiaryCommentLikeService {

    private final MemberRepository memberRepository;
    private final DiaryCommentRepository commentRepository;
    private final DiaryCommentLikeRepository likeRepository;
    private final MemberGuard memberGuard;

    public DiaryCommentLikeResponseDTO toggleLike(Long commentId) {
        Long memberId = SecurityUtil.getCurrentMemberId();

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        DiaryComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentHandler(ErrorStatus.COMMENT_NOT_FOUND));

        memberGuard.checkOwnerIsNotDeleted(comment.getMember());
        memberGuard.checkOwnerIsNotDeleted(comment.getDiary().getMember());

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
                .memberProfile(MemberProfileDTO.from(member))
                .liked(liked)
                .likeCount(comment.getLikeCount())
                .build();
    }
}



