package org.lxdproject.lxd.diarycommentlike.repository;

import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.diary.entity.QDiary;
import org.lxdproject.lxd.diarycomment.entity.QDiaryComment;
import org.lxdproject.lxd.diarycommentlike.entity.QDiaryCommentLike;
import org.lxdproject.lxd.diarylike.entity.QDiaryLike;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
public class DiaryCommentLikeRepositoryImpl implements DiaryCommentLikeRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final EntityManager entityManager;

    private static final QDiary DIARY = QDiary.diary;
    private static final QDiaryCommentLike DIARY_COMMENT_LIKE = QDiaryCommentLike.diaryCommentLike;
    private static final QDiaryComment DIARY_COMMENT = QDiaryComment.diaryComment;

    @Override
    public void softDeleteDiaryCommentLikes(Long memberId, LocalDateTime localDateTime) {

        // 기존 변경 먼저 DB 반영
        entityManager.flush();

        // 벌크 연산 (DB 직접 업데이트)
        queryFactory
                .update(DIARY_COMMENT_LIKE)
                .set(DIARY_COMMENT_LIKE.deletedAt, localDateTime)
                .where(DIARY_COMMENT_LIKE.member.id.eq(memberId)
                        .or(DIARY_COMMENT_LIKE.comment.member.id.eq(memberId)))
                .execute();

        // 캐시 비우기
        entityManager.clear();
    }
}
