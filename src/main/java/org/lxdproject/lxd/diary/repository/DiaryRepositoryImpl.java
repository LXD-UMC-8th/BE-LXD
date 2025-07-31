package org.lxdproject.lxd.diary.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.common.util.DateFormatUtil;
import org.lxdproject.lxd.diary.dto.*;
import org.lxdproject.lxd.diary.entity.Diary;
import org.lxdproject.lxd.diary.entity.QDiary;
import org.lxdproject.lxd.diary.entity.enums.Language;
import org.lxdproject.lxd.diary.entity.enums.Visibility;
import org.lxdproject.lxd.diarylike.entity.QDiaryLike;
import org.lxdproject.lxd.member.entity.QFriendship;
import org.lxdproject.lxd.member.entity.QMember;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.lxdproject.lxd.diary.util.DiaryUtil.generateContentPreview;
import static org.lxdproject.lxd.member.entity.QMember.member;

@RequiredArgsConstructor
public class DiaryRepositoryImpl implements DiaryRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    QDiary diary = QDiary.diary;
    QDiaryLike diaryLike = QDiaryLike.diaryLike;
    QFriendship friendship = QFriendship.friendship;

    @Override
    public MyDiarySliceResponseDTO findMyDiaries(Long userId, Boolean likedOnly, Pageable pageable) {
        List<Diary> diaries = queryFactory
                .selectFrom(diary)
                .leftJoin(diary.likes, diaryLike)
                .where(
                        diary.member.id.eq(userId),
                        diary.deletedAt.isNull(),
                        likedOnly != null && likedOnly ? diaryLike.member.id.eq(userId) : null
                )
                .distinct()
                .orderBy(diary.createdAt.desc())
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
                        .build())
                .toList();

        return MyDiarySliceResponseDTO.builder()
                .diaries(content)
                .page(pageable.getPageNumber())
                .size(pageable.getPageSize())
                .hasNext(hasNext)
                .build();
    }

    public MyDiarySliceResponseDTO getDiariesByMemberId(Long userId, Long memberId, Pageable pageable) {
        Set<Long> friendIds = getFriendIds(userId);

        BooleanExpression visibilityCondition;

        if (userId.equals(memberId)) { // 본인
            visibilityCondition = diary.visibility.in(Visibility.PUBLIC, Visibility.FRIENDS, Visibility.PRIVATE);
        } else if (friendIds.contains(memberId)) { // 친구관계
            visibilityCondition = diary.visibility.in(Visibility.PUBLIC, Visibility.FRIENDS);
        } else { // 타인
            visibilityCondition = diary.visibility.eq(Visibility.PUBLIC);
        }

        List<Diary> diaries = queryFactory
                .selectFrom(diary)
                .leftJoin(diary.likes, diaryLike).fetchJoin()
                .where(
                        diary.member.id.eq(memberId),
                        diary.deletedAt.isNull(),
                        visibilityCondition
                )
                .distinct()
                .orderBy(diary.createdAt.desc())
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
                        .build())
                .toList();

        return MyDiarySliceResponseDTO.builder()
                .diaries(content)
                .page(pageable.getPageNumber())
                .size(pageable.getPageSize())
                .hasNext(hasNext)
                .build();
    }


    @Override
    public List<DiaryStatsResponseDTO> getDiaryStatsByMonth(Long userId, int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = YearMonth.of(year, month).atEndOfMonth();

        var dateExpression = Expressions.stringTemplate("DATE({0})", diary.createdAt);

        return queryFactory
                .select(dateExpression, diary.count())
                .from(diary)
                .where(
                        diary.member.id.eq(userId),
                        diary.deletedAt.isNull(),
                        diary.createdAt.between(start.atStartOfDay(), end.atTime(23, 59, 59))
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

                    return new DiaryStatsResponseDTO(date, tuple.get(diary.count()));
                })
                .toList();
    }

    @Override
    public DiarySliceResponseDTO findDiariesOfFriends(Long userId, Pageable pageable) {
        QDiary diary = QDiary.diary;
        QMember member = QMember.member;

        Set<Long> friendIds = getFriendIds(userId);

        List<Diary> diaries = queryFactory
                .selectFrom(diary)
                .leftJoin(diary.member, member).fetchJoin()
                .where(
                        diary.member.id.in(friendIds),
                        diary.visibility.ne(Visibility.PRIVATE),
                        diary.deletedAt.isNull()
                )
                .orderBy(diary.createdAt.desc())
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
                        .createdAt(d.getCreatedAt().toString())
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
    public DiarySliceResponseDTO findLikedDiariesOfFriends(Long userId, Pageable pageable) {
        QDiaryLike diaryLike = QDiaryLike.diaryLike;
        QDiary diary = QDiary.diary;
        QMember member = QMember.member;
        QFriendship friendship = QFriendship.friendship;

        List<Long> likedDiaryIds = queryFactory
                .select(diaryLike.diary.id)
                .from(diaryLike)
                .where(diaryLike.member.id.eq(userId))
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
                .selectFrom(diary)
                .join(diary.member, member).fetchJoin()
                .where(
                        diary.id.in(likedDiaryIds),
                        diary.member.id.ne(userId),
                        diary.deletedAt.isNull(),
                        diary.visibility.eq(Visibility.PUBLIC)
                                .or(
                                        diary.visibility.eq(Visibility.FRIENDS)
                                                .and(diary.member.id.in(friendIds))
                                )
                )
                .orderBy(diary.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        boolean hasNext = diaries.size() > pageable.getPageSize();
        if (hasNext) diaries.remove(diaries.size() - 1);

        List<DiarySummaryResponseDTO> dtoList = diaries.stream()
                .map(d -> DiarySummaryResponseDTO.builder()
                        .diaryId(d.getId())
                        .createdAt(d.getCreatedAt().toString())
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
        Set<Long> friendIds = getFriendIds(userId);

        BooleanBuilder condition = new BooleanBuilder();
        condition.or(diary.visibility.eq(Visibility.PUBLIC));
        condition.or(diary.visibility.eq(Visibility.FRIENDS).and(diary.member.id.in(friendIds)));
        condition.or(diary.visibility.eq(Visibility.PRIVATE).and(diary.member.id.eq(userId)));

        if (language != null) {
            condition.and(diary.language.eq(language));
        }

        List<Diary> diaries = queryFactory
                .selectFrom(diary)
                .join(diary.member, member).fetchJoin()
                .where(condition, diary.deletedAt.isNull())
                .orderBy(diary.createdAt.desc())
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
                        .diaryId(d.getId())
                        .createdAt(d.getCreatedAt().toString())
                        .title(d.getTitle())
                        .visibility(d.getVisibility())
                        .thumbnailUrl(d.getThumbImg())
                        .likeCount(d.getLikeCount())
                        .commentCount(d.getCommentCount())
                        .correctionCount(d.getCorrectionCount())
                        .contentPreview(generateContentPreview(d.getContent()))
                        .language(d.getLanguage())
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
                .select(friendship.receiver.id)
                .from(friendship)
                .where(friendship.requester.id.eq(userId), friendship.deletedAt.isNull())
                .fetch();

        List<Long> receivedFriendIds = queryFactory
                .select(friendship.requester.id)
                .from(friendship)
                .where(friendship.receiver.id.eq(userId), friendship.deletedAt.isNull())
                .fetch();

        Set<Long> friendIds = new HashSet<>();
        friendIds.addAll(sentFriendIds);
        friendIds.addAll(receivedFriendIds);

        return friendIds;
    }
}


