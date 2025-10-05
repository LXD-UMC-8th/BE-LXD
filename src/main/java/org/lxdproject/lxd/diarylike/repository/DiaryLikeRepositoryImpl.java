package org.lxdproject.lxd.diarylike.repository;

import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.diary.entity.QDiary;
import org.lxdproject.lxd.diarycomment.entity.QDiaryComment;
import org.lxdproject.lxd.diarylike.entity.QDiaryLike;

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

    private static final QDiaryLike DIARY_LIKE = QDiaryLike.diaryLike;

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

    @Override
    public void softDeleteDiaryLikes(Long memberId, LocalDateTime localDateTime) {

        // 기존 변경 먼저 DB 반영
        entityManager.flush();

        // 벌크 연산 (DB 직접 업데이트)
        queryFactory
                .update(DIARY_LIKE)
                .set(DIARY_LIKE.deletedAt, localDateTime)
                .where(DIARY_LIKE.member.id.eq(memberId))
                .execute();

        entityManager.clear();
    }

}
