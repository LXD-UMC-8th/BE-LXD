package org.lxdproject.lxd.notification.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.member.entity.QMember;
import org.lxdproject.lxd.notification.entity.Notification;
import org.lxdproject.lxd.notification.entity.QNotification;

import java.util.List;

@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    QNotification notification = QNotification.notification;
    QMember sender = QMember.member;

    public List<Notification> findNotificationsWithMemberId(Long memberId, Boolean isRead, int page, int size) {
        BooleanBuilder where = new BooleanBuilder();
        where.and(notification.receiver.id.eq(memberId));

        if (isRead != null) {
            where.and(notification.isRead.eq(isRead));
        }

        return queryFactory
                .selectFrom(notification)
                .join(notification.sender, sender).fetchJoin()
                .where(where)
                .orderBy(notification.id.desc())
                .offset((long) page * size)
                .limit(size + 1) // hasNext 판별 위해 1개 더 조회
                .fetch();
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
}
