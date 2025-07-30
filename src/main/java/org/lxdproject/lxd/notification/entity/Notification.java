package org.lxdproject.lxd.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import org.lxdproject.lxd.common.entity.BaseEntity;
import org.lxdproject.lxd.member.entity.Member;
import org.lxdproject.lxd.notification.entity.enums.NotificationType;
import org.lxdproject.lxd.notification.entity.enums.TargetType;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 알림 수신자, 받는 사람
    @ManyToOne(fetch = FetchType.LAZY) // 회원과 n:1 연관관계
    @JoinColumn(name = "receiver_id", nullable = false)
    private Member receiver;

    // 알림 발신자, 보낸 사람
    @ManyToOne(fetch = FetchType.LAZY) // 회원과 n:1 연관관계
    @JoinColumn(name = "sender_id", nullable = false)
    private Member sender;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 30)
    private NotificationType notificationType;

    @Column(nullable = false)
    @Builder.Default
    private boolean isRead = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", length = 30, nullable = false)
    private TargetType targetType;

    @Column(nullable = false)
    private Long targetId;

    // SSE로 전달 완료 여부
    @Column(nullable = false)
    @Builder.Default
    private boolean isDelivered = false;

    @Column(nullable = false)
    private String redirectUrl;

    public void markAsRead(){
        this.isRead = true;
    }
}
