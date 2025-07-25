package org.lxdproject.lxd.diary.repository.DiaryRepository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.diary.dto.DiarySummaryResponseDto;
import org.lxdproject.lxd.diary.entity.Diary;
import org.lxdproject.lxd.diary.entity.QDiary;
import org.lxdproject.lxd.diary.entity.mapping.QDiaryLike;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.List;

@RequiredArgsConstructor
public class DiaryRepositoryImpl implements DiaryRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    QDiary diary = QDiary.diary;
    QDiaryLike diaryLike = QDiaryLike.diaryLike;

    @Override
    public Slice<DiarySummaryResponseDto> findMyDiaries(Long userId, Boolean likedOnly, Pageable pageable) {
        List<Diary> diaries = queryFactory
                .selectFrom(diary)
                .leftJoin(diary.likes, diaryLike)
                .where(
                        diary.member.id.eq(userId),
                        likedOnly != null && likedOnly ? diaryLike.member.id.eq(userId) : null
                )
                .distinct()
                .orderBy(diary.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        List<DiarySummaryResponseDto> content = diaries.stream()
                .map(d -> DiarySummaryResponseDto.builder()
                        .diaryId(d.getId())
                        .createdAt(d.getCreatedAt())
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

        boolean hasNext = content.size() > pageable.getPageSize();
        if (hasNext) content = content.subList(0, pageable.getPageSize());

        return new SliceImpl<>(content, pageable, hasNext);
    }

    private String generatePreview(String content) {
        if (content == null) return "";
        return content.length() <= 100 ? content : content.substring(0, 100);
    }
}

