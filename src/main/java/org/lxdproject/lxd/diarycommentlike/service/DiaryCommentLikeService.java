package org.lxdproject.lxd.diarycommentlike.service;

import lombok.RequiredArgsConstructor;
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

    public DiaryCommentLikeResponseDTO toggleLike(Long memberId, Long diaryId, Long commentId) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        DiaryComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다."));


        if (!comment.getDiary().getId().equals(diaryId)) {
            throw new IllegalArgumentException("해당 댓글은 지정된 일기에 속하지 않습니다.");
        }

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



