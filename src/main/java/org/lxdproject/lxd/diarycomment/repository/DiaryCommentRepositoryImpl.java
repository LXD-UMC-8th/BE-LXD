package org.lxdproject.lxd.diarycomment.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.diarycomment.entity.DiaryComment;
import org.lxdproject.lxd.diarycomment.entity.QDiaryComment;
import org.lxdproject.lxd.diarycommentlike.entity.QDiaryCommentLike;
import org.lxdproject.lxd.member.entity.QMember;

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
}
