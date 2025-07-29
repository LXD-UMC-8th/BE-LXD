package org.lxdproject.lxd.diary.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.correction.util.DateFormatUtil;
import org.lxdproject.lxd.diary.dto.*;
import org.lxdproject.lxd.diary.entity.Diary;
import org.lxdproject.lxd.diary.entity.QDiary;
import org.lxdproject.lxd.diary.entity.enums.Visibility;
import org.lxdproject.lxd.diary.entity.mapping.QDiaryLike;
import org.lxdproject.lxd.member.entity.QFriendship;
import org.lxdproject.lxd.member.entity.QMember;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class DiaryRepositoryImpl implements DiaryRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    QDiary diary = QDiary.diary;
    QDiaryLike diaryLike = QDiaryLike.diaryLike;

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
                .limit(pageable.getPageSize() + 1) // +1로 hasNext 판별
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
                        .contentPreview(generatePreview(d.getContent()))
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

    private String generatePreview(String content) {
        if (content == null) return "";
        return content.length() <= 100 ? content : content.substring(0, 100);
    }


    @Override
    public List<DiaryStatsResponseDTO> getDiaryStatsByMonth(Long userId, int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = YearMonth.of(year, month).atEndOfMonth();

        // created_at을 string 타입으로 변환
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
    public List<Diary> findByMemberIdAndVisibilityForViewer(Long memberId, boolean isFriend) {
        QDiary diary = QDiary.diary;

        BooleanExpression visibilityCondition = diary.visibility.eq(Visibility.PUBLIC);
        if (isFriend) {
            visibilityCondition = visibilityCondition.or(diary.visibility.eq(Visibility.FRIENDS));
        }

        return queryFactory
                .selectFrom(diary)
                .where(
                        diary.member.id.eq(memberId),
                        diary.deletedAt.isNull(),
                        visibilityCondition
                )
                .orderBy(diary.createdAt.desc())
                .fetch();
    }

    @Override
    public DiarySliceResponseDTO findDiariesOfFriends(Long userId, Pageable pageable) {
        QDiary diary = QDiary.diary;
        QMember member = QMember.member;
        QFriendship friendship = QFriendship.friendship;

        // 친구 ID 목록 조회 (양방향)
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

        // 친구들이 작성한 공개/친구공개 일기 조회
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
                        .contentPreview(d.getContent().substring(0, Math.min(30, d.getContent().length())))
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

        // 1. 내가 좋아요 누른 일기 중
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

        // 2. 친구 목록
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

        // 3. 조건 필터링된 일기 조회
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
                        .contentPreview(d.getContent().substring(0, Math.min(30, d.getContent().length())))
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
}


