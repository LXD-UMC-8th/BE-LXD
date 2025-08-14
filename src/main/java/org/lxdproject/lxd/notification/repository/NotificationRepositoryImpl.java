package org.lxdproject.lxd.notification.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.member.entity.QMember;
import org.lxdproject.lxd.notification.entity.Notification;
import org.lxdproject.lxd.notification.entity.QNotification;
import org.lxdproject.lxd.notification.entity.enums.NotificationType;
import org.lxdproject.lxd.notification.entity.enums.TargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    QNotification notification = QNotification.notification;
    QMember sender = QMember.member;

    @Override
    public Page<Notification> findPageByMemberId(Long memberId, Boolean isRead, Pageable pageable) {
        BooleanBuilder where = new BooleanBuilder()
                .and(notification.receiver.id.eq(memberId));
        if (isRead != null) {
            where.and(notification.isRead.eq(isRead));
        }

        // fetch join한 contentQuery
        List<Notification> content = queryFactory
                .selectFrom(notification)
                .join(notification.sender, sender).fetchJoin()
                .where(where)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(notification.id.desc())
                .fetch();

        // countQuery
        long count = queryFactory
                .select(notification.count())
                .from(notification)
                .where(where)
                .fetchOne();

        return new PageImpl<>(content, pageable, count);
    }


    @Override
    public List<Notification> findUnreadWithSenderByReceiverId(Long receiverId) {

        return queryFactory
                .selectFrom(notification)
                .leftJoin(notification.sender, sender).fetchJoin()
                .where(
                        notification.receiver.id.eq(receiverId),
                        notification.isRead.isFalse()
                )
                .orderBy(notification.createdAt.desc())
                .fetch();
    }

    @Override
    public long deleteFriendRequestNotification(Long receiverId, Long requesterId) {
        return queryFactory.delete(notification)
                .where(
                        notification.receiver.id.eq(receiverId),
                        notification.notificationType.eq(NotificationType.FRIEND_REQUEST),
                        notification.targetType.eq(TargetType.MEMBER),
                        notification.targetId.eq(requesterId)
                )
                .execute();
    }

}
