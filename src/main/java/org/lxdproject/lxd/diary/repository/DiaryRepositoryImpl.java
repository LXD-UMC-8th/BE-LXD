package org.lxdproject.lxd.diary.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.authz.predicate.VisibilityPredicates;
import org.lxdproject.lxd.common.util.DateFormatUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.lxdproject.lxd.diary.dto.*;
import org.lxdproject.lxd.diary.entity.Diary;
import org.lxdproject.lxd.diary.entity.enums.Language;
import org.lxdproject.lxd.diary.entity.enums.Visibility;

import org.lxdproject.lxd.diary.entity.QDiary;
import org.lxdproject.lxd.diarylike.entity.QDiaryLike;
import org.lxdproject.lxd.friend.entity.QFriendship;
import org.lxdproject.lxd.member.entity.QMember;

import static org.lxdproject.lxd.diary.util.DiaryUtil.generateContentPreview;


@RequiredArgsConstructor
public class DiaryRepositoryImpl implements DiaryRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final VisibilityPredicates visibilityPredicates;

    private static final QDiary DIARY = QDiary.diary;
    private static final QDiaryLike DIARY_LIKE = QDiaryLike.diaryLike;
    private static final QFriendship FRIENDSHIP = QFriendship.friendship;
    private static final QMember MEMBER = QMember.member;

    @Override
    public MyDiarySliceResponseDTO findMyDiaries(Long userId, Boolean likedOnly, Pageable pageable) {

        Set<Long> likedSet = getLikedDiaryIdSet(userId);

        List<Diary> diaries = queryFactory
                .selectFrom(DIARY)
                .leftJoin(DIARY.likes, DIARY_LIKE)
                .where(
                        DIARY.member.id.eq(userId),
                        DIARY.deletedAt.isNull(),
                        likedOnly != null && likedOnly ? DIARY_LIKE.member.id.eq(userId) : null
                )
                .distinct()
                .orderBy(DIARY.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        boolean hasNext = diaries.size() > pageable.getPageSize();
        if (hasNext) {
            diaries = diaries.subList(0, pageable.getPageSize());
        }

        List<MyDiarySummaryResponseDTO> content = diaries.stream()
                .map(d -> MyDiarySummaryResponseDTO.builder()
                        .diaryId(d.getId())
                        .createdAt(DateFormatUtil.formatDate(d.getCreatedAt()))
                        .title(d.getTitle())
                        .visibility(d.getVisibility())
                        .thumbnailUrl(d.getThumbImg())
                        .likeCount(d.getLikeCount())
                        .commentCount(d.getCommentCount())
                        .correctionCount(d.getCorrectionCount())
                        .contentPreview(generateContentPreview(d.getContent()))
                        .language(d.getLanguage())
                        .liked(likedSet.contains(d.getId()))
                        .build())
                .toList();

        return MyDiarySliceResponseDTO.builder()
                .diaries(content)
                .page(pageable.getPageNumber() + 1)
                .size(pageable.getPageSize())
                .hasNext(hasNext)
                .build();
    }

    public Page<Diary> getDiariesByMemberId(Long viewerId, Long ownerId, Set<Long> friendIds, Pageable pageable){
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
    public List<DiaryStatsResponseDTO> getDiaryStatsByMonth(Long userId, int year, int month) {
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
    public DiarySliceResponseDTO findDiariesOfFriends(Long userId, Pageable pageable) {

        Set<Long> likedSet = getLikedDiaryIdSet(userId);
        Set<Long> friendIds = getFriendIds(userId);

        List<Diary> diaries = queryFactory
                .selectFrom(DIARY)
                .leftJoin(DIARY.member, MEMBER).fetchJoin()
                .where(
                        DIARY.member.id.in(friendIds),
                        DIARY.visibility.ne(Visibility.PRIVATE),
                        DIARY.deletedAt.isNull()
                )
                .orderBy(DIARY.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        boolean hasNext = diaries.size() > pageable.getPageSize();
        if (hasNext) {
            diaries.remove(diaries.size() - 1);
        }

        List<DiarySummaryResponseDTO> dtoList = diaries.stream()
                .map(d -> DiarySummaryResponseDTO.builder()
                        .diaryId(d.getId())
                        .createdAt(DateFormatUtil.formatDate(d.getCreatedAt()))
                        .title(d.getTitle())
                        .visibility(d.getVisibility())
                        .thumbnailUrl(d.getThumbImg())
                        .likeCount(d.getLikeCount())
                        .commentCount(d.getCommentCount())
                        .correctionCount(d.getCorrectionCount())
                        .contentPreview(generateContentPreview(d.getContent()))
                        .language(d.getLanguage())
                        .writerUsername(d.getMember().getUsername())
                        .writerNickname(d.getMember().getNickname())
                        .writerProfileImg(d.getMember().getProfileImg())
                        .writerId(d.getMember().getId())
                        .liked(likedSet.contains(d.getId()))
                        .build())
                .toList();

        return DiarySliceResponseDTO.builder()
                .diaries(dtoList)
                .page(pageable.getPageNumber() + 1)
                .size(pageable.getPageSize())
                .hasNext(hasNext)
                .build();
    }

    @Override
    public DiarySliceResponseDTO findLikedDiaries(Long userId, Pageable pageable) {

        Set<Long> likedSet = getLikedDiaryIdSet(userId);
        List<Long> likedDiaryIds = queryFactory
                .select(DIARY_LIKE.diary.id)
                .from(DIARY_LIKE)
                .where(DIARY_LIKE.member.id.eq(userId))
                .fetch();

        if (likedDiaryIds.isEmpty()) {
            return DiarySliceResponseDTO.builder()
                    .diaries(List.of())
                    .page(pageable.getPageNumber() + 1)
                    .size(pageable.getPageSize())
                    .hasNext(false)
                    .build();
        }

        Set<Long> friendIds = getFriendIds(userId);

        List<Diary> diaries = queryFactory
                .selectFrom(DIARY)
                .join(DIARY.member, MEMBER).fetchJoin()
                .where(
                        DIARY.id.in(likedDiaryIds),
                        DIARY.member.id.ne(userId),
                        DIARY.deletedAt.isNull(),
                        DIARY.visibility.eq(Visibility.PUBLIC)
                                .or(
                                        DIARY.visibility.eq(Visibility.FRIENDS)
                                                .and(DIARY.member.id.in(friendIds))
                                )
                )
                .orderBy(DIARY.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        boolean hasNext = diaries.size() > pageable.getPageSize();
        if (hasNext) diaries.remove(diaries.size() - 1);

        List<DiarySummaryResponseDTO> dtoList = diaries.stream()
                .map(d -> DiarySummaryResponseDTO.builder()
                        .diaryId(d.getId())
                        .createdAt(DateFormatUtil.formatDate(d.getCreatedAt()))
                        .title(d.getTitle())
                        .visibility(d.getVisibility())
                        .thumbnailUrl(d.getThumbImg())
                        .likeCount(d.getLikeCount())
                        .commentCount(d.getCommentCount())
                        .correctionCount(d.getCorrectionCount())
                        .contentPreview(generateContentPreview(d.getContent()))
                        .language(d.getLanguage())
                        .writerUsername(d.getMember().getUsername())
                        .writerNickname(d.getMember().getNickname())
                        .writerProfileImg(d.getMember().getProfileImg())
                        .writerId(d.getMember().getId())
                        .liked(likedSet.contains(d.getId()))
                        .build()
                )
                .toList();

        return DiarySliceResponseDTO.builder()
                .diaries(dtoList)
                .page(pageable.getPageNumber() + 1)
                .size(pageable.getPageSize())
                .hasNext(hasNext)
                .build();
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

    private Set<Long> getFriendIds(Long userId) {
        List<Long> sentFriendIds = queryFactory
                .select(FRIENDSHIP.receiver.id)
                .from(FRIENDSHIP)
                .where(FRIENDSHIP.requester.id.eq(userId), FRIENDSHIP.deletedAt.isNull())
                .fetch();

        List<Long> receivedFriendIds = queryFactory
                .select(FRIENDSHIP.requester.id)
                .from(FRIENDSHIP)
                .where(FRIENDSHIP.receiver.id.eq(userId), FRIENDSHIP.deletedAt.isNull())
                .fetch();

        Set<Long> friendIds = new HashSet<>();
        friendIds.addAll(sentFriendIds);
        friendIds.addAll(receivedFriendIds);

        return friendIds;
    }

    public Set<Long> getLikedDiaryIdSet(Long memberId) {
        if (memberId == null) return Set.of();

        List<Long> likedDiaryIds = queryFactory
                .select(DIARY_LIKE.diary.id)
                .from(DIARY_LIKE)
                .where(DIARY_LIKE.member.id.eq(memberId))
                .fetch();

        return new HashSet<>(likedDiaryIds);
    }

}


