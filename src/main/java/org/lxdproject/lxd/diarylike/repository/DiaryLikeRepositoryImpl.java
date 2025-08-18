package org.lxdproject.lxd.diarylike.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.diarylike.entity.QDiaryLike;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class DiaryLikeRepositoryImpl implements DiaryLikeRepositoryCustom {

    private final JPAQueryFactory queryFactory;

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

}
