package org.lxdproject.lxd.notification.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.correction.util.DateFormatUtil;
import org.lxdproject.lxd.member.entity.QMember;
import org.lxdproject.lxd.notification.dto.NotificationResponseDTO;
import org.lxdproject.lxd.notification.entity.Notification;
import org.lxdproject.lxd.notification.entity.QNotification;

import java.util.List;

@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    QNotification notification = QNotification.notification;
    QMember sender = QMember.member;

    @Override
    public List<NotificationResponseDTO> findNotificationsWithCursor(Long memberId, Boolean isRead, Long lastId, int size) {
        BooleanBuilder where = new BooleanBuilder();
        where.and(notification.receiver.id.eq(memberId));

        if (isRead != null) {
            where.and(notification.isRead.eq(isRead)); // 읽음 여부 조건
        }

        if (lastId != null) {
            where.and(notification.id.lt(lastId)); // 커서 조건 -> ID보다 작은 알림만
        }

        List<Tuple> results = queryFactory
                .select(notification.id, sender.profileImg, sender.nickname, sender.username,
                        notification.message, notification.redirectUrl, notification.isRead, notification.createdAt)
                .from(notification)
                .join(notification.sender, sender)
                .where(where)
                .orderBy(notification.id.desc())
                .limit(size)
                .fetch();

        return results.stream()
                .map(tuple -> new NotificationResponseDTO(
                        tuple.get(notification.id),
                        tuple.get(sender.profileImg),
                        tuple.get(sender.nickname),
                        tuple.get(sender.username),
                        tuple.get(notification.message),
                        tuple.get(notification.redirectUrl),
                        tuple.get(notification.isRead),
                        DateFormatUtil.formatDate(tuple.get(notification.createdAt))
                ))
                .toList();
    }

    @Override
    public List<Notification> findUnreadWithSenderByReceiverId(Long receiverId) {
        QNotification notification = QNotification.notification;
        QMember sender = QMember.member;

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
