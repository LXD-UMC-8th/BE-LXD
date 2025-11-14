package org.lxdproject.lxd.domain.diarycomment.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.domain.diary.entity.QDiary;
import org.lxdproject.lxd.domain.diarycomment.entity.QDiaryComment;
import org.lxdproject.lxd.domain.member.entity.QMember;
import org.lxdproject.lxd.global.authz.predicate.MemberPredicates;
import org.lxdproject.lxd.domain.diarycomment.entity.DiaryComment;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class DiaryCommentRepositoryImpl implements DiaryCommentRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final EntityManager entityManager;
    private final MemberPredicates memberPredicates;

    private static final QDiary DIARY = QDiary.diary;
    private static final QDiaryComment DIARY_COMMENT = QDiaryComment.diaryComment;
    private static final QMember MEMBER = QMember.member;

    @Override
    public List<DiaryComment> findParentComments(Long diaryId, int offset, int size) {
        BooleanExpression condition = DIARY_COMMENT.diary.id.eq(diaryId)
                .and(DIARY_COMMENT.parent.isNull())
                .and(memberPredicates.isNotDeleted(DIARY_COMMENT.member));

        return queryFactory
                .selectFrom(DIARY_COMMENT)
                .leftJoin(DIARY_COMMENT.member, MEMBER).fetchJoin()
                .where(condition)
                .orderBy(DIARY_COMMENT.createdAt.asc())
                .offset(offset)
                .limit(size)
                .fetch();
    }

    @Override
    public List<DiaryComment> findRepliesByParentIds(List<Long> parentIds) {
        BooleanExpression condition = DIARY_COMMENT.parent.id.in(parentIds)
                .and(memberPredicates.isNotDeleted(MEMBER));

        return queryFactory
                .selectFrom(DIARY_COMMENT)
                .leftJoin(DIARY_COMMENT.member, MEMBER).fetchJoin()
                .leftJoin(DIARY_COMMENT.parent).fetchJoin()
                .where(condition)
                .orderBy(DIARY_COMMENT.createdAt.asc())
                .fetch();
    }

    @Override
    public Long countParentComments(Long diaryId) {
        return queryFactory
                .select(DIARY_COMMENT.count())
                .from(DIARY_COMMENT)
                .where(DIARY_COMMENT.diary.id.eq(diaryId)
                        .and(DIARY_COMMENT.parent.isNull())
                        .and(DIARY_COMMENT.deletedAt.isNull()) // 삭제된 댓글은 count 에서 제외
                        .and(memberPredicates.isNotDeleted(DIARY_COMMENT.member))) // 부모 댓글의 주인이 탈퇴했다면 count 에서 제외
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
                .select(DIARY_COMMENT.diary.id)
                .from(DIARY_COMMENT)
                .where(DIARY_COMMENT.member.id.eq(memberId))
                .distinct()
                .fetch();

        Set<Long> affectedDiaryIds = new HashSet<>();
        affectedDiaryIds.addAll(diaryIdsOfMember);
        affectedDiaryIds.addAll(ownCommentDiaryIds);


        // 연관된 일기가 없을 시 메서드 종료
        if (affectedDiaryIds.isEmpty()) return;

        // 기존 변경 먼저 DB 반영
        entityManager.flush();

        // soft delete (회원이 쓴 댓글 + 회원의 일기에 달린 댓글)
        queryFactory.update(DIARY_COMMENT)
                .set(DIARY_COMMENT.deletedAt, deletedAt)
                .where(
                        DIARY_COMMENT.deletedAt.isNull()
                                        .and(
                                                DIARY_COMMENT.member.id.eq(memberId) // 탈퇴한 회원이 쓴 댓글 soft delete
                                                        .or(DIARY_COMMENT.diary.member.id.eq(memberId)) ) // 탈퇴한 회원의 일기에 달린 댓글 soft delete
                                        )
                .execute();

        // 캐시 비우기
        entityManager.clear();

        // 일기 엔티티의 countComment 업데이트
        affectedDiaryIds.forEach(diaryId -> {
            Long commentCount = Optional.ofNullable(queryFactory
                    .select(Wildcard.count)
                    .from(DIARY_COMMENT)
                    .where(DIARY_COMMENT.diary.id.eq(diaryId)
                            .and(DIARY_COMMENT.deletedAt.isNull()))
                    .fetchOne()).orElse(0L);

            queryFactory
                    .update(DIARY)
                    .set(DIARY.commentCount, commentCount.intValue())
                    .where(DIARY.id.eq(diaryId))
                    .execute();
        });

        /**
         * 삭제한 댓글이 대댓글이라면, 삭제한 댓글의 부모 댓글의 replyCount 값도 업데이트 해줘야한다.
         */

        /**
         * 삭제한 댓글을 List 목록으로 가져온다.
         * (탈퇴한 회원이 댓글 작성자일 때와 탈퇴한 회원이 작성한 일기의 댓글들 가져오기)
         */
        List<Long> deletedCommentIds = queryFactory
                .select(DIARY_COMMENT.id)
                .from(DIARY_COMMENT)
                .where(
                        DIARY_COMMENT.deletedAt.eq(deletedAt)
                                .and(DIARY_COMMENT.member.id.eq(memberId)
                                        .or(DIARY_COMMENT.diary.member.id.eq(memberId)))

                )
                .fetch();

        /**
         * 삭제한 댓글의 모든 부모 댓글을 Tuple로 가져온다
         * Tuple -> (댓글 아이디, 해당 아이디의 count(*))
         *
         * 단, 대댓글이 아닌 경우(부모 댓글이 없는 경우)는 가져오지 않는다.
         */
        List<Tuple> parentDeletedComments = queryFactory
                .select(DIARY_COMMENT.parent.id, DIARY_COMMENT.count())
                .from(DIARY_COMMENT)
                .where(DIARY_COMMENT.parent.isNotNull()
                        .and(DIARY_COMMENT.id.in(deletedCommentIds)))
                .groupBy(DIARY_COMMENT.parent.id)
                .fetch();

        if (!parentDeletedComments.isEmpty()) {
            NumberExpression<Integer> replyCaseExpr = Expressions.asNumber(0);

            for (Tuple tuple : parentDeletedComments) {
                Long parentId = tuple.get(DIARY_COMMENT.parent.id);
                Long deletedReplyCount = tuple.get(DIARY_COMMENT.count());

                // CaseBuilder로 조건별 계산식 구성
                replyCaseExpr = replyCaseExpr.add(
                        new CaseBuilder()
                                .when(DIARY_COMMENT.id.eq(parentId))
                                .then(DIARY_COMMENT.replyCount.subtract(deletedReplyCount.intValue()))
                                .otherwise(0)
                );
            }

            queryFactory.update(DIARY_COMMENT)
                    .set(DIARY_COMMENT.replyCount, replyCaseExpr)
                    .where(DIARY_COMMENT.id.in(
                            parentDeletedComments.stream()
                                    .map(t -> t.get(DIARY_COMMENT.parent.id))
                                    .collect(Collectors.toList())))
                    .execute();
        }


    }

    @Override
    public void hardDeleteDiaryCommentsOlderThanThreshold(LocalDateTime threshold) {
        // purge 안 된 탈퇴 회원 조회
        List<Long> withdrawnMemberIds = queryFactory
                .select(MEMBER.id)
                .from(MEMBER)
                .where(MEMBER.deletedAt.isNotNull()
                        .and(MEMBER.deletedAt.loe(threshold))
                        .and(MEMBER.isPurged.isFalse()))
                .fetch();

        if (withdrawnMemberIds.isEmpty()) return;

        entityManager.flush();

        // 탈퇴 회원이 작성한 댓글 + 탈퇴 회원의 일기에 달린 모든 댓글 삭제
        queryFactory.delete(DIARY_COMMENT)
                .where(
                        DIARY_COMMENT.member.id.in(withdrawnMemberIds)
                                .or(DIARY_COMMENT.diary.member.id.in(withdrawnMemberIds))
                )
                .execute();

        entityManager.clear();
    }

    // CaseBuilder로 최적화 하기
    @Override
    public void recoverDiaryCommentsByMemberIdAndDeletedAt(Long memberId, LocalDateTime deletedAt) {

        // 회원이 탈퇴한 시간에 삭제된 '회원이 작성한 일기 id' 가져오기
        List<Long> diaryIdsOfMember = queryFactory
                .select(DIARY.id)
                .from(DIARY)
                .where(DIARY.member.id.eq(memberId)
                        .and(DIARY.deletedAt.eq(deletedAt)))
                .fetch();

        // 회원이 탈퇴한 시간에 삭제된 '회원이 작성한 댓글의 일기 id' 가져오기
        List<Long> ownCommentDiaryIds = queryFactory
                .select(DIARY_COMMENT.diary.id)
                .from(DIARY_COMMENT)
                .where(DIARY_COMMENT.member.id.eq(memberId)
                        .and(DIARY_COMMENT.deletedAt.eq(deletedAt)))
                .distinct()
                .fetch();

        Set<Long> affectedDiaryIds = new HashSet<>();
        affectedDiaryIds.addAll(diaryIdsOfMember);
        affectedDiaryIds.addAll(ownCommentDiaryIds);


        // 연관된 일기가 없을 시 메서드 종료
        if (affectedDiaryIds.isEmpty()) return;


        /**
         * 아래 쪽의 replyCount 계산을 위해 복구할 댓글의 아이디 목록을 미리 가져오기
         *
         * 복구할 댓글을 List 목록으로 가져온다.
         * (복구할 회원이 댓글 작성자일 때와 복구할 회원이 작성한 일기의 댓글들 가져오기)
         */
        List<Long> deletedCommentIds = queryFactory
                .select(DIARY_COMMENT.id)
                .from(DIARY_COMMENT)
                .where(
                        DIARY_COMMENT.deletedAt.eq(deletedAt)
                                .and(DIARY_COMMENT.member.id.eq(memberId)
                                        .or(DIARY_COMMENT.diary.member.id.eq(memberId)))

                )
                .fetch();

        // 기존 변경 먼저 DB 반영
        entityManager.flush();

        // 댓글 복구 (회원이 쓴 댓글 + 회원의 일기에 달린 댓글)
        queryFactory.update(DIARY_COMMENT)
                .set(DIARY_COMMENT.deletedAt, (LocalDateTime)null)
                .where(
                        DIARY_COMMENT.deletedAt.eq(deletedAt)
                                .and(
                                        DIARY_COMMENT.member.id.eq(memberId) // 탈퇴한 회원이 쓴 댓글 soft delete
                                                .or(DIARY_COMMENT.diary.member.id.eq(memberId))// 탈퇴한 회원의 일기에 달린 댓글 soft delete
                                )
                )
                .execute();

        // 캐시 비우기
        entityManager.clear();

        // 일기 엔티티의 countComment 업데이트
        affectedDiaryIds.forEach(diaryId -> {
            Long commentCount = Optional.ofNullable(queryFactory
                    .select(Wildcard.count)
                    .from(DIARY_COMMENT)
                    .where(DIARY_COMMENT.diary.id.eq(diaryId)
                            .and(DIARY_COMMENT.deletedAt.isNull()))
                    .fetchOne()).orElse(0L);

            queryFactory
                    .update(DIARY)
                    .set(DIARY.commentCount, commentCount.intValue())
                    .where(DIARY.id.eq(diaryId))
                    .execute();
        });

        /**
         * 복구한 댓글이 대댓글이라면, 복구한 댓글의 부모 댓글의 replyCount 값도 업데이트 해줘야한다.
         */

        /**
         * 복구한 댓글의 모든 부모 댓글을 Tuple로 가져온다
         * Tuple -> (댓글 아이디, 해당 아이디의 count(*))
         *
         * 단, 대댓글이 아닌 경우(부모 댓글이 없는 경우)는 가져오지 않는다.
         */
        List<Tuple> parentDeletedComments = queryFactory
                .select(DIARY_COMMENT.parent.id, DIARY_COMMENT.count())
                .from(DIARY_COMMENT)
                .where(DIARY_COMMENT.parent.isNotNull()
                        .and(DIARY_COMMENT.id.in(deletedCommentIds)))
                .groupBy(DIARY_COMMENT.parent.id)
                .fetch();

        /**
         * 위에서 가져온 Tuple 정보를 Map에 저장한다.
         * Map -> key: 부모 댓글 아이디 , value: 증가된 대댓글 개수
         */
        Map<Long, Long> modifiedParentCommentIds =
                parentDeletedComments.stream()
                        .collect(Collectors.toMap(
                                t -> t.get(DIARY_COMMENT.parent.id),
                                t -> t.get(DIARY_COMMENT.count())
                        ));

        // Map에 저장된 부모 댓글 Entity 가져오기
        List<DiaryComment> parents = queryFactory
                .selectFrom(DIARY_COMMENT)
                .where(DIARY_COMMENT.id.in(modifiedParentCommentIds.keySet()))
                .fetch();

        // Map에 저장된 댓글 개수 만큼 각각 replyCount 증가
        parents.forEach(parent -> {
            int delta = modifiedParentCommentIds.get(parent.getId()).intValue();
            parent.increaseReplyCount(delta);
        });
    }

}
