package org.lxdproject.lxd.diarycommentlike.service;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.diarycomment.entity.DiaryComment;
import org.lxdproject.lxd.diarycomment.repository.DiaryCommentRepository;
import org.lxdproject.lxd.diarycommentlike.dto.DiaryCommentLikeResponseDTO;
import org.lxdproject.lxd.diarycommentlike.entity.DiaryCommentLike;
import org.lxdproject.lxd.diarycommentlike.repository.DiaryCommentLikeRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DiaryCommentLikeService {

    private final DiaryCommentLikeRepository likeRepository;
    private final DiaryCommentRepository commentRepository;

    public DiaryCommentLikeResponseDTO toggleLike(Long memberId, Long diaryId, Long commentId) {

        DiaryComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다."));

        if (!comment.getDiaryId().equals(diaryId)) {
            throw new IllegalArgumentException("해당 댓글은 지정된 일기에 속하지 않습니다.");
        }

        Optional<DiaryCommentLike> existing = likeRepository.findByMemberIdAndComment_Id(memberId, commentId);
        boolean liked;

        if (existing.isPresent()) {
            likeRepository.delete(existing.get());
            comment.decreaseLikeCount(); // likeCount 감소
            liked = false;
        } else {
            likeRepository.save(DiaryCommentLike.builder()
                    .memberId(memberId)
                    .comment(comment)
                    .build());
            comment.increaseLikeCount(); // likeCount 증가
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

