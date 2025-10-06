package org.lxdproject.lxd.diarylike.repository;

import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.diary.entity.QDiary;
import org.lxdproject.lxd.diarycomment.entity.QDiaryComment;
import org.lxdproject.lxd.diarylike.entity.QDiaryLike;
import org.lxdproject.lxd.member.entity.QMember;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class DiaryLikeRepositoryImpl implements DiaryLikeRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final EntityManager entityManager;

    private static final QDiary DIARY = QDiary.diary;
    private static final QDiaryLike DIARY_LIKE = QDiaryLike.diaryLike;
    private static final QMember member = QMember.member;

    @Override
    public Set<Long> findLikedDiaryIdSet(Long memberId) {
        return queryFactory
                .select(DIARY_LIKE.diary.id)
                .from(DIARY_LIKE)
                .where(DIARY_LIKE.member.id.eq(memberId))
                .fetch()
                .stream()
                .collect(Collectors.toSet());
    }

    @Override
    public List<Long> findLikedDiaryIdList(Long memberId) {
        return queryFactory
                .select(DIARY_LIKE.diary.id)
                .from(DIARY_LIKE)
                .where(DIARY_LIKE.member.id.eq(memberId))
                .fetch();
    }

    // 규모 커지면 루프 기반 update 에서 native 쿼리로 최적화 고려
    @Override
    public void softDeleteDiaryLikes(Long memberId, LocalDateTime localDateTime) {

        // 탈퇴한 회원이 작성한 일기 id 가져오기
        List<Long> writtenDiaryIds = queryFactory
                .select(DIARY.id)
                .from(DIARY)
                .where(DIARY.member.id.eq(memberId))
                .fetch();

        // 탈퇴한 회원이 좋아요를 누른 일기 id 가져오기
        List<Long> likedDiaryIds = queryFactory
                .select(DIARY_LIKE.diary.id)
                .from(DIARY_LIKE)
                .where(DIARY_LIKE.member.id.eq(memberId))
                .distinct()
                .fetch();

        Set<Long> affectedDiaryIds = new HashSet<>();
        affectedDiaryIds.addAll(writtenDiaryIds);
        affectedDiaryIds.addAll(likedDiaryIds);

        // 연관된 일기가 없으면 메서드 종료
        if (affectedDiaryIds.isEmpty()) return;

        // 기존 변경 먼저 DB 반영
        entityManager.flush();

        // soft delete (탈퇴한 회원이 누른 일기 좋아요 + 탈퇴한 회원의 일기가 받은 좋아요)
        queryFactory.update(DIARY_LIKE)
                .set(DIARY_LIKE.deletedAt, localDateTime)
                .where(
                        DIARY_LIKE.member.id.eq(memberId) // 탈퇴한 회원이 누른 좋아요
                                .or(DIARY_LIKE.diary.id.in(writtenDiaryIds)) // 탈퇴한 회원의 일기가 받은 좋아요
                )
                .execute();

        // 캐시 비우기
        entityManager.clear();

        // 일기 엔티티의 likeCount 업데이트
        affectedDiaryIds.forEach(diaryId -> {
            Long likeCount = Optional.ofNullable(queryFactory
                    .select(Wildcard.count)
                    .from(DIARY_LIKE)
                    .where(DIARY_LIKE.diary.id.eq(diaryId)
                            .and(DIARY_LIKE.deletedAt.isNull()))
                    .fetchOne()).orElse(0L);

            queryFactory.update(DIARY)
                    .set(DIARY.likeCount, likeCount.intValue())
                    .where(DIARY.id.eq(diaryId))
                    .execute();
        });

    }

    @Override
    public void hardDeleteDiaryLikesOlderThanThreshold(LocalDateTime threshold) {

        // purge 안 된 탈퇴 회원 조회
        List<Long> withdrawnMemberIds = queryFactory
                .select(member.id)
                .from(member)
                .where(member.deletedAt.isNotNull()
                        .and(member.deletedAt.loe(threshold))
                        .and(member.isPurged.isFalse()))
                .fetch();

        if (withdrawnMemberIds.isEmpty()) return;

        entityManager.flush();

        // 탈퇴 회원 누른 일기 좋아요 + 탈퇴 회원의 일기에 달린 모든 일기 좋아요 삭제
        queryFactory.delete(DIARY_LIKE)
                .where(
                        DIARY_LIKE.member.id.in(withdrawnMemberIds)
                                .or(DIARY_LIKE.diary.member.id.in(withdrawnMemberIds))
                )
                .execute();

        entityManager.clear();

    }

}
