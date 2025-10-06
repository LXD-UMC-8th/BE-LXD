package org.lxdproject.lxd.diarycommentlike.repository;

import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.diary.entity.QDiary;
import org.lxdproject.lxd.diarycomment.entity.QDiaryComment;
import org.lxdproject.lxd.diarycommentlike.entity.QDiaryCommentLike;
import org.lxdproject.lxd.diarylike.entity.QDiaryLike;
import org.lxdproject.lxd.member.entity.QMember;

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
    private static final QDiaryComment COMMENT = QDiaryComment.diaryComment;
    private static final QDiaryCommentLike DIARY_COMMENT_LIKE = QDiaryCommentLike.diaryCommentLike;
    private static final QDiaryComment DIARY_COMMENT = QDiaryComment.diaryComment;
    private static final QMember member = QMember.member;

    // 규모 커지면 루프 기반 update 에서 native 쿼리로 최적화 고려
    @Override
    public void softDeleteDiaryCommentLikes(Long memberId, LocalDateTime localDateTime) {

        // 탈퇴한 회원이 작성한 일기 댓글 id 가져오기
        List<Long> writtenDiaryCommentIds = queryFactory
                .select(COMMENT.id)
                .from(COMMENT)
                .where(COMMENT.member.id.eq(memberId))
                .fetch();

        // 탈퇴한 회원이 좋아요를 누른 댓글 id 가져오기
        List<Long> likedDiaryCommentIds = queryFactory
                .select(DIARY_COMMENT_LIKE.comment.id)
                .from(DIARY_COMMENT_LIKE)
                .where(DIARY_COMMENT_LIKE.member.id.eq(memberId))
                .distinct()
                .fetch();

        Set<Long> affectedCommentIds = new HashSet<>();
        affectedCommentIds.addAll(writtenDiaryCommentIds);
        affectedCommentIds.addAll(likedDiaryCommentIds);

        // 연관된 일기가 없을 시 메서드 종료
        if (affectedCommentIds.isEmpty()) return;

        // 기존 변경 먼저 DB 반영
        entityManager.flush();

        // soft delete (탈퇴한 회원이 누른 일기 좋아요 + 탈퇴한 회원이 작성한 일기에 있는 좋아요)
        queryFactory.update(DIARY_COMMENT_LIKE)
                .set(DIARY_COMMENT_LIKE.deletedAt, localDateTime)
                .where(
                        DIARY_COMMENT_LIKE.member.id.eq(memberId) // 탈퇴한 회원이 누른 일기 좋아요
                                .or(DIARY_COMMENT_LIKE.comment.id.in(writtenDiaryCommentIds)) // 탈퇴한 회원이 작성한 일기의 좋아요
                )
                .execute();

        // 캐시 비우기
        entityManager.clear();

        // 일기 댓글 엔티티의 likeCount 업데이트
        affectedCommentIds.forEach(commentId -> {
            Long likeCount = Optional.ofNullable(queryFactory
                    .select(Wildcard.count)
                    .from(DIARY_COMMENT_LIKE)
                    .where(DIARY_COMMENT_LIKE.comment.id.eq(commentId)
                            .and(DIARY_COMMENT_LIKE.deletedAt.isNull()))
                    .fetchOne()).orElse(0L);

            queryFactory.update(DIARY_COMMENT)
                    .set(DIARY_COMMENT.likeCount, likeCount.intValue())
                    .where(DIARY_COMMENT.id.eq(commentId))
                    .execute();
        });
    }

    @Override
    public void hardDeleteDiaryCommentLikesOlderThanThreshold(LocalDateTime threshold) {

        // purge 안 된 탈퇴 회원 조회
        List<Long> withdrawnMemberIds = queryFactory
                .select(member.id)
                .from(member)
                .where(member.deletedAt.isNotNull()
                        .and(member.deletedAt.loe(threshold))
                        .and(member.isPurged.isFalse()))
                .fetch();

        if (withdrawnMemberIds.isEmpty()) return;

        entityManager.flush();

        // 탈퇴한 회원이 누른 댓글 좋아요 + 탈퇴한 회원이 작성한 댓글에 눌린 댓글 좋아요
        queryFactory.delete(DIARY_COMMENT_LIKE)
                .where(
                        DIARY_COMMENT_LIKE.member.id.in(withdrawnMemberIds)
                                .or(DIARY_COMMENT_LIKE.comment.member.id.in(withdrawnMemberIds))
                )
                .execute();

        entityManager.clear();

    }
}
