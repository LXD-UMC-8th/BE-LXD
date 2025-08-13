package org.lxdproject.lxd.diary.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.common.util.DateFormatUtil;
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

    public MyDiarySliceResponseDTO getDiariesByMemberId(Long userId, Long memberId, Pageable pageable) {

        Set<Long> likedSet = getLikedDiaryIdSet(userId);
        Set<Long> friendIds = getFriendIds(userId);
        BooleanExpression visibilityCondition;

        if (userId.equals(memberId)) { // 본인
            visibilityCondition = DIARY.visibility.in(Visibility.PUBLIC, Visibility.FRIENDS, Visibility.PRIVATE);
        } else if (friendIds.contains(memberId)) { // 친구관계
            visibilityCondition = DIARY.visibility.in(Visibility.PUBLIC, Visibility.FRIENDS);
        } else { // 타인
            visibilityCondition = DIARY.visibility.eq(Visibility.PUBLIC);
        }

        List<Diary> diaries = queryFactory
                .selectFrom(DIARY)
                .leftJoin(DIARY.likes, DIARY_LIKE).fetchJoin()
                .where(
                        DIARY.member.id.eq(memberId),
                        DIARY.deletedAt.isNull(),
                        visibilityCondition
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
    public DiarySliceResponseDTO findExploreDiaries(Long userId, Language language, Pageable pageable) {
        Set<Long> likedSet = getLikedDiaryIdSet(userId);
        Set<Long> friendIds = getFriendIds(userId);

        // visibilityCondition : PUBLIC or FRIENDS and 친구관계
        BooleanBuilder visibilityCondition = new BooleanBuilder()
                .or(DIARY.visibility.eq(Visibility.PUBLIC))
                .or(DIARY.visibility.eq(Visibility.FRIENDS)
                        .and(DIARY.member.id.in(friendIds)));

        // 내 일기 제외 + 삭제 안 됨 + visibilityCondition
        BooleanBuilder condition = new BooleanBuilder()
                .and(DIARY.member.id.ne(userId))
                .and(DIARY.deletedAt.isNull())
                .and(visibilityCondition);

        if (language != null) {
            condition.and(DIARY.language.eq(language));
        }

        List<Diary> diaries = queryFactory
                .selectFrom(DIARY)
                .join(DIARY.member, MEMBER).fetchJoin()
                .where(condition, DIARY.deletedAt.isNull())
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
                        .writerUsername(d.getMember().getUsername())
                        .writerNickname(d.getMember().getNickname())
                        .writerProfileImg(d.getMember().getProfileImg())
                        .writerId(d.getMember().getId())
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

        return DiarySliceResponseDTO.builder()
                .diaries(dtoList)
                .page(pageable.getPageNumber() + 1)
                .size(pageable.getPageSize())
                .hasNext(hasNext)
                .build();
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

    private Set<Long> getLikedDiaryIdSet(Long userId) {
        List<Long> likedDiaryIds = queryFactory
                .select(DIARY_LIKE.diary.id)
                .from(DIARY_LIKE)
                .where(DIARY_LIKE.member.id.eq(userId))
                .fetch();
        return new HashSet<>(likedDiaryIds);
    }
}


