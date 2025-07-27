package org.lxdproject.lxd.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lxdproject.lxd.apiPayload.code.exception.handler.MemberHandler;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.config.security.SecurityUtil;
import org.lxdproject.lxd.member.entity.Member;
import org.lxdproject.lxd.member.repository.MemberRepository;
import org.lxdproject.lxd.notification.dto.NotificationPublishEvent;
import org.lxdproject.lxd.notification.dto.NotificationRequestDTO;
import org.lxdproject.lxd.notification.dto.NotificationResponseDTO;
import org.lxdproject.lxd.notification.entity.Notification;
import org.lxdproject.lxd.notification.publisher.NotificationPublisher;
import org.lxdproject.lxd.notification.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationPublisher notificationPublisher;
    private final MemberRepository memberRepository;

    public void saveAndPublishNotification(NotificationRequestDTO dto) {

        Member receiver = memberRepository.findById(dto.getReceiverId())
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        Member sender = memberRepository.findById(SecurityUtil.getCurrentMemberId()).orElse(null);

        // Todo. NotificationType 별로 메시지 규격화 하기
        String message = "알림 메시지";

        // Notification 저장
        Notification notification = Notification.builder()
                .receiver(receiver)
                .sender(sender)
                .notificationType(dto.getNotificationType())
                .message(message)
                .targetType(dto.getTargetType())
                .targetId(dto.getTargetId())
                .redirectUrl(dto.getRedirectUrl())
                .build();

        notificationRepository.save(notification);

        // Notification을 기반으로 Redis에 발행되는 메시지 생성
        NotificationPublishEvent publishEventDTO = NotificationPublishEvent.from(notification);

        // Redis에 publish
        notificationPublisher.publish(publishEventDTO);
        log.info("[알림 발행] to memberId: {}, notificationType: {}, message: {}", dto.getReceiverId(), dto.getNotificationType(), message);
    }

    public Page<NotificationResponseDTO> getNotifications(Long memberId, Boolean isRead, Pageable pageable) {
        Page<Notification> notifications;

        if (isRead == null) {
            notifications = notificationRepository.findAllByReceiverId(memberId, pageable);
        } else {
            notifications = notificationRepository.findAllByReceiverIdAndIsRead(memberId, isRead, pageable);
        }

        return notifications.map(notification -> {
            Member sender = notification.getSender(); // fetch join or lazy
            return NotificationResponseDTO.builder()
                    .profileImg(sender.getProfileImg())
                    .nickname(sender.getNickname())
                    .username(sender.getUsername())
                    .message(notification.getMessage())
                    .redirectUrl(notification.getRedirectUrl())
                    .isRead(notification.isRead())
                    .build();
        });
    }
}

