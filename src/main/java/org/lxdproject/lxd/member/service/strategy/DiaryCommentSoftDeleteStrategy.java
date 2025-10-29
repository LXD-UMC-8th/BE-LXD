package org.lxdproject.lxd.member.service.strategy;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.diarycomment.repository.DiaryCommentRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DiaryCommentSoftDeleteStrategy implements SoftDeleteStrategy {

    private final DiaryCommentRepository diaryCommentRepository;

    @Override
    public void softDelete(Long memberId, LocalDateTime deletedAt) {
        // 일기 댓글 soft delete
        diaryCommentRepository.softDeleteMemberComments(memberId, deletedAt);
    }

}
