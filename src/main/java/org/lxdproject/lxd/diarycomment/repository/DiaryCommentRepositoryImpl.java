package org.lxdproject.lxd.diarycomment.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.authz.predicate.DiaryPredicates;
import org.lxdproject.lxd.authz.predicate.MemberPredicates;
import org.lxdproject.lxd.diary.entity.QDiary;
import org.lxdproject.lxd.diarycomment.entity.DiaryComment;
import org.lxdproject.lxd.diarycomment.entity.QDiaryComment;
import org.lxdproject.lxd.diarycommentlike.entity.QDiaryCommentLike;
import org.lxdproject.lxd.member.entity.QMember;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
public class DiaryCommentRepositoryImpl implements DiaryCommentRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final EntityManager entityManager;

    QDiary DIARY = QDiary.diary;
    QDiaryComment comment = QDiaryComment.diaryComment;
    QMember member = QMember.member;

    @Override
    public List<DiaryComment> findParentComments(Long diaryId, int offset, int size) {
        BooleanExpression condition = comment.diary.id.eq(diaryId)
                .and(comment.parent.isNull())
                .and(MemberPredicates.isNotDeleted(comment.member));

        return queryFactory
                .selectFrom(comment)
                .leftJoin(comment.member, member).fetchJoin()
                .where(condition)
                .offset(offset)
                .limit(size)
                .fetch();
    }

    @Override
    public List<DiaryComment> findRepliesByParentIds(List<Long> parentIds) {
        BooleanExpression condition = comment.parent.id.in(parentIds)
                .and(MemberPredicates.isNotDeleted(member));

        return queryFactory
                .selectFrom(comment)
                .leftJoin(comment.member, member).fetchJoin()
                .leftJoin(comment.parent).fetchJoin()
                .where(condition)
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

    // TODO 추후 caseBuilder로 최적화 하기!
    @Override
    public void softDeleteMemberComments(Long memberId, LocalDateTime deletedAt) {
        // 탈퇴한 회원이 작성한 일기 id 가져오기
        List<Long> diaryIdsOfMember = queryFactory
                .select(DIARY.id)
                .from(DIARY)
                .where(DIARY.member.id.eq(memberId))
                .fetch();

        // 탈퇴한 회원이 작성한 댓글의 일기 id 가져오기
        List<Long> ownCommentDiaryIds = queryFactory
                .select(comment.diary.id)
                .from(comment)
                .where(comment.member.id.eq(memberId)) .distinct()
                .fetch();

        Set<Long> affectedDiaryIds = new HashSet<>();
        affectedDiaryIds.addAll(diaryIdsOfMember);
        affectedDiaryIds.addAll(ownCommentDiaryIds);


        // 연관된 일기가 없을 시 메서드 종료
        if (affectedDiaryIds.isEmpty()) return;

        // 기존 변경 먼저 DB 반영
        entityManager.flush();

        // soft delete (회원이 쓴 댓글 + 회원의 일기에 달린 댓글)
        queryFactory.update(comment)
                .set(comment.deletedAt, deletedAt)
                .where(comment.member.id.eq(memberId) // 탈퇴한 회원이 쓴 댓글 soft delete
                        .or(comment.diary.member.id.eq(memberId)) ) // 탈퇴한 회원의 일기에 달린 댓글 soft delete
                .execute();

        // 캐시 비우기
        entityManager.clear();

        // 일기 엔티티의 countComment 업데이트
        affectedDiaryIds.forEach(diaryId -> {
            Long commentCount = Optional.ofNullable(queryFactory
                    .select(Wildcard.count)
                    .from(comment)
                    .where(comment.diary.id.eq(diaryId)
                            .and(comment.deletedAt.isNull()))
                    .fetchOne()).orElse(0L);

            queryFactory
                    .update(DIARY)
                    .set(DIARY.commentCount, commentCount.intValue())
                    .where(DIARY.id.eq(diaryId))
                    .execute();
        });
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
