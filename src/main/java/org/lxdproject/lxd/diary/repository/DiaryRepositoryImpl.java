package org.lxdproject.lxd.diary.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.authz.predicate.VisibilityPredicates;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;

import org.lxdproject.lxd.diary.dto.*;
import org.lxdproject.lxd.diary.entity.Diary;
import org.lxdproject.lxd.diary.entity.enums.Language;

import org.lxdproject.lxd.diary.entity.QDiary;
import org.lxdproject.lxd.diarylike.entity.QDiaryLike;
import org.lxdproject.lxd.friend.entity.QFriendship;
import org.lxdproject.lxd.member.entity.QMember;

@RequiredArgsConstructor
public class DiaryRepositoryImpl implements DiaryRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final VisibilityPredicates visibilityPredicates;

    private static final QDiary DIARY = QDiary.diary;
    private static final QDiaryLike DIARY_LIKE = QDiaryLike.diaryLike;
    private static final QFriendship FRIENDSHIP = QFriendship.friendship;
    private static final QMember MEMBER = QMember.member;

    @Override
    public Page<Diary> findMyDiaries(Long memberId, Boolean likedOnly, Pageable pageable) {
        BooleanExpression baseCondition = DIARY.member.id.eq(memberId)
                .and(DIARY.deletedAt.isNull());

        // likedOnly 활용해 일기 좋아요 여부 분리
        BooleanExpression likedFilter = (likedOnly != null && likedOnly)
                ? DIARY_LIKE.member.id.eq(memberId)
                : null;

        List<Diary> diaries = queryFactory
                .selectFrom(DIARY)
                .leftJoin(DIARY.likes, DIARY_LIKE)
                .where(baseCondition, likedFilter)
                .distinct()
                .orderBy(DIARY.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(Wildcard.count)
                .from(DIARY)
                .leftJoin(DIARY.likes, DIARY_LIKE)
                .where(baseCondition, likedFilter)
                .fetchOne();

        return new PageImpl<>(diaries, pageable, total == null ? 0L : total);
    }

    @Override
    public Page<Diary> findDiariesByMemberId(Long viewerId, Long ownerId, Set<Long> friendIds, Pageable pageable){
        BooleanExpression visibility = VisibilityPredicates.diaryVisibleToOthers(viewerId, DIARY, friendIds);
        BooleanExpression condition = DIARY.member.id.eq(ownerId)
                .and(DIARY.deletedAt.isNull())
                .and(visibility);

        List<Diary> diaries = queryFactory
                .selectFrom(DIARY)
                .join(DIARY.member, MEMBER).fetchJoin()
                .where(condition)
                .orderBy(DIARY.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(Wildcard.count)
                .from(DIARY)
                .where(condition)
                .fetchOne();

        return new PageImpl<>(diaries, pageable, total == null ? 0L : total);
    }

    @Override
    public List<DiaryStatsResponseDTO> findDiaryStatsByMonth(Long userId, int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = YearMonth.of(year, month).atEndOfMonth();

        var dateExpression = Expressions.stringTemplate("DATE({0})", DIARY.createdAt);

        return queryFactory
                .select(dateExpression, DIARY.count())
                .from(DIARY)
                .where(
                        DIARY.member.id.eq(userId),
                        DIARY.deletedAt.isNull(),
                        DIARY.createdAt.between(start.atStartOfDay(), end.atTime(23, 59, 59))
                )
                .groupBy(dateExpression)
                .orderBy(dateExpression.asc())
                .fetch()
                .stream()
                .map(tuple -> {
                    Object dateObj = tuple.get(dateExpression);
                    String date = (dateObj instanceof java.sql.Date)
                            ? ((java.sql.Date) dateObj).toLocalDate().toString()
                            : dateObj.toString();

                    return new DiaryStatsResponseDTO(date, tuple.get(DIARY.count()));
                })
                .toList();
    }

    @Override
    public Page<Diary> findFriendDiaries(Long memberId, Set<Long> friendIds, Pageable pageable) {
        BooleanExpression visibility = VisibilityPredicates.diaryVisibleToOthers(memberId, DIARY, friendIds);

        BooleanExpression condition = DIARY.member.id.in(friendIds)
                .and(DIARY.deletedAt.isNull())
                .and(visibility);

        List<Diary> diaries = queryFactory
                .selectFrom(DIARY)
                .join(DIARY.member, MEMBER).fetchJoin()
                .where(condition)
                .orderBy(DIARY.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(Wildcard.count)
                .from(DIARY)
                .where(condition)
                .fetchOne();

        return new PageImpl<>(diaries, pageable, total == null ? 0L : total);
    }

    @Override
    public Page<Diary> findLikedDiaries(Long memberId, List<Long> likedDiaryIds, Set<Long> friendIds, Pageable pageable) {
        if (likedDiaryIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        BooleanExpression visibility = VisibilityPredicates.diaryVisibleToOthers(memberId, DIARY, friendIds);

        BooleanExpression condition = DIARY.id.in(likedDiaryIds)
                .and(DIARY.deletedAt.isNull())
                .and(visibility);

        List<Diary> diaries = queryFactory
                .selectFrom(DIARY)
                .join(DIARY.member, MEMBER).fetchJoin()
                .where(condition)
                .orderBy(DIARY.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(Wildcard.count)
                .from(DIARY)
                .where(condition)
                .fetchOne();

        return new PageImpl<>(diaries, pageable, total == null ? 0L : total);
    }

    @Override
    public Page<Diary> findExploreDiaries(Long memberId, Language language, Set<Long> friendIds, Pageable pageable) {

        BooleanExpression visibility = visibilityPredicates.diaryVisibleToOthers(memberId, DIARY, friendIds);
        BooleanExpression condition = DIARY.deletedAt.isNull().and(visibility);
        if (language != null) {
            condition = condition.and(DIARY.language.eq(language));
        }

        List<Diary> content = queryFactory
                .selectFrom(DIARY)
                .join(DIARY.member, MEMBER).fetchJoin()
                .where(condition)
                .orderBy(DIARY.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(Wildcard.count)
                .from(DIARY)
                .where(condition)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0L : total);
    }

}
