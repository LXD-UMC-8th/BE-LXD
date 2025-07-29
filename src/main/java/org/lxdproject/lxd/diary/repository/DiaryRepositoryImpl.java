package org.lxdproject.lxd.diary.repository;

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
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

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
    public List<Diary> findByMemberIdAndVisibilityForFriend(Long friendId) {
        return queryFactory
                .selectFrom(diary)
                .where(
                        diary.member.id.eq(friendId),
                        diary.deletedAt.isNull(),
                        diary.visibility.in(Visibility.PUBLIC, Visibility.FRIENDS)
                )
                .orderBy(diary.createdAt.desc())
                .fetch();
    }
}


