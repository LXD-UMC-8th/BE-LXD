package org.lxdproject.lxd.diarycomment.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.diarycomment.entity.DiaryComment;
import org.lxdproject.lxd.diarycomment.entity.QDiaryComment;
import org.lxdproject.lxd.diarycommentlike.entity.QDiaryCommentLike;
import org.lxdproject.lxd.member.entity.QMember;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class DiaryCommentRepositoryImpl implements DiaryCommentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    QDiaryComment comment = QDiaryComment.diaryComment;
    QMember member = QMember.member;

    @Override
    public List<DiaryComment> findParentComments(Long diaryId, int offset, int size) {
        return queryFactory
                .selectFrom(comment)
                .leftJoin(comment.member, member).fetchJoin()
                .where(comment.diary.id.eq(diaryId)
                        .and(comment.parent.isNull()))
                .orderBy(comment.createdAt.asc())
                .offset(offset)
                .limit(size)
                .fetch();
    }

    @Override
    public List<DiaryComment> findRepliesByParentIds(List<Long> parentIds) {
        return queryFactory
                .selectFrom(comment)
                .leftJoin(comment.member, member).fetchJoin()
                .leftJoin(comment.parent).fetchJoin()
                .where(comment.parent.id.in(parentIds))
                .orderBy(comment.createdAt.asc())
                .fetch();
    }

    @Override
    public Long countParentComments(Long diaryId) {
        return queryFactory
                .select(comment.count())
                .from(comment)
                .where(comment.diary.id.eq(diaryId)
                        .and(comment.parent.isNull()))
                .fetchOne();
    }

    @Override
    public void softDeleteMemberComments(Long memberId, LocalDateTime deletedAt) {
        // 해당 회원이 작성한 댓글 + 그 회원의 일기에 달린 모든 댓글 삭제
        queryFactory.update(comment)
                .set(comment.deletedAt, deletedAt)
                .where(comment.member.id.eq(memberId)
                        .or(comment.diary.member.id.eq(memberId)))
                .execute();
    }

    @Override
    public void hardDeleteWithdrawnMemberComments(LocalDateTime threshold) {
        // purge 안 된 탈퇴 회원 조회
        List<Long> withdrawnMemberIds = queryFactory
                .select(member.id)
                .from(member)
                .where(member.deletedAt.isNotNull()
                        .and(member.deletedAt.loe(threshold))
                        .and(member.isPurged.isFalse()))
                .fetch();

        if (withdrawnMemberIds.isEmpty()) return;

        // 탈퇴 회원이 작성한 댓글 + 탈퇴 회원의 일기에 달린 모든 댓글 삭제
        queryFactory.delete(comment)
                .where(
                        comment.member.id.in(withdrawnMemberIds)
                                .or(comment.diary.member.id.in(withdrawnMemberIds))
                )
                .execute();
    }

}
