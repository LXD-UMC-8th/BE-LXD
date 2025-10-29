package org.lxdproject.lxd.member.service.strategy.softDelete;

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
        // 탈퇴자가 작성한 일기 댓글 및 탈퇴자가 작성한 일기에 달린 댓글 soft delete
        diaryCommentRepository.softDeleteMemberComments(memberId, deletedAt);
    }

}
