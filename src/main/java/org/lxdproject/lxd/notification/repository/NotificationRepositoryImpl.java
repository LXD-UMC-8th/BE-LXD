package org.lxdproject.lxd.notification.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.member.entity.QMember;
import org.lxdproject.lxd.notification.entity.Notification;
import org.lxdproject.lxd.notification.entity.QNotification;
import org.lxdproject.lxd.notification.entity.enums.NotificationType;
import org.lxdproject.lxd.notification.entity.enums.TargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final EntityManager entityManager;

    QNotification NOTIFICATION = QNotification.notification;
    QMember MEMBER = QMember.member;

    @Override
    public Page<Notification> findPageByMemberId(Long memberId, Boolean isRead, Pageable pageable) {
        BooleanBuilder where = new BooleanBuilder()
                .and(NOTIFICATION.receiver.id.eq(memberId));
        if (isRead != null) {
            where.and(NOTIFICATION.isRead.eq(isRead));
        }

        // fetch join한 contentQuery
        List<Notification> content = queryFactory
                .selectFrom(NOTIFICATION)
                .join(NOTIFICATION.sender, MEMBER).fetchJoin()
                .where(where)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(NOTIFICATION.createdAt.desc())
                .fetch();

        // countQuery
        long count = queryFactory
                .select(NOTIFICATION.count())
                .from(NOTIFICATION)
                .where(where)
                .fetchOne();

        return new PageImpl<>(content, pageable, count);
    }


    @Override
    public List<Notification> findUnreadWithSenderByReceiverId(Long receiverId) {

        return queryFactory
                .selectFrom(NOTIFICATION)
                .leftJoin(NOTIFICATION.sender, MEMBER).fetchJoin()
                .where(
                        NOTIFICATION.receiver.id.eq(receiverId),
                        NOTIFICATION.isRead.isFalse()
                )
                .orderBy(NOTIFICATION.createdAt.desc())
                .fetch();
    }

    @Override
    public Long findFriendRequestNotificationId(Long receiverId, Long requesterId) {
        return queryFactory.select(NOTIFICATION.id)
                .from(NOTIFICATION)
                .where(
                        NOTIFICATION.receiver.id.eq(receiverId),
                        NOTIFICATION.notificationType.eq(NotificationType.FRIEND_REQUEST),
                        NOTIFICATION.targetType.eq(TargetType.MEMBER),
                        NOTIFICATION.targetId.eq(requesterId)
                )
                .fetchOne();
    }

    @Override
    public void hardDeleteNotificationsOlderThanThreshold(LocalDateTime threshold) {
        // 탈퇴 후 아직 purged 전인 회원 조회
        List<Long> withdrawnMemberIds = queryFactory
                .select(MEMBER.id)
                .from(MEMBER)
                .where(MEMBER.deletedAt.isNotNull()
                        .and(MEMBER.deletedAt.loe(threshold))
                        .and(MEMBER.isPurged.isFalse()))
                .fetch();

        if (withdrawnMemberIds.isEmpty()) return;

        entityManager.flush();

        // 탈퇴한 멤버가 보낸/받은 알림 전체 삭제
        queryFactory
                .delete(NOTIFICATION)
                .where(
                        NOTIFICATION.sender.id.in(withdrawnMemberIds)
                                .or(NOTIFICATION.receiver.id.in(withdrawnMemberIds))
                )
                .execute();

        entityManager.clear();
    }

    @Override
    public long deleteFriendRequestNotification(Long receiverId, Long requesterId) {
        return queryFactory.delete(NOTIFICATION)
                .where(
                        NOTIFICATION.receiver.id.eq(receiverId),
                        NOTIFICATION.notificationType.eq(NotificationType.FRIEND_REQUEST),
                        NOTIFICATION.targetType.eq(TargetType.MEMBER),
                        NOTIFICATION.targetId.eq(requesterId)
                )
                .execute();
    }

}
