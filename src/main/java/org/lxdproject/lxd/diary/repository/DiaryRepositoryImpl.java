package org.lxdproject.lxd.diary.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.correction.util.DateFormatUtil;
import org.lxdproject.lxd.diary.dto.DiarySliceResponseDTO;
import org.lxdproject.lxd.diary.dto.DiaryStatsResponseDTO;
import org.lxdproject.lxd.diary.dto.DiarySummaryResponseDTO;
import org.lxdproject.lxd.diary.entity.Diary;
import org.lxdproject.lxd.diary.entity.QDiary;
import org.lxdproject.lxd.diary.entity.enums.Visibility;
import org.lxdproject.lxd.diary.entity.mapping.QDiaryLike;
import org.lxdproject.lxd.member.entity.QFriendship;
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
    public DiarySliceResponseDTO findMyDiaries(Long userId, Boolean likedOnly, Pageable pageable) {
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

        List<DiarySummaryResponseDTO> content = diaries.stream()
                .map(d -> DiarySummaryResponseDTO.builder()
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

        return DiarySliceResponseDTO.builder()
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
        QFriendship friendship = QFriendship.friendship;

        // 1. 친구 ID 목록 조회 (양방향)
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

        // 2. 친구들의 공개/친구공개 일기만 필터링
        List<Diary> diaries = queryFactory
                .selectFrom(diary)
                .where(
                        diary.member.id.in(friendIds),
                        diary.visibility.ne(Visibility.PRIVATE),
                        diary.deletedAt.isNull()
                )
                .orderBy(diary.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        // 3. 페이징 처리
        boolean hasNext = diaries.size() > pageable.getPageSize();
        if (hasNext) {
            diaries.remove(diaries.size() - 1);
        }

        // 4. DTO 변환
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
                        .build())
                .toList();

        return DiarySliceResponseDTO.builder()
                .diaries(dtoList)
                .page(pageable.getPageNumber() + 1)
                .size(pageable.getPageSize())
                .hasNext(hasNext)
                .build();
    }
}


