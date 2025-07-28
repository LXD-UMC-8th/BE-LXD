package org.lxdproject.lxd.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lxdproject.lxd.apiPayload.code.exception.handler.MemberHandler;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.config.security.SecurityUtil;
import org.lxdproject.lxd.member.entity.Member;
import org.lxdproject.lxd.member.repository.MemberRepository;
import org.lxdproject.lxd.notification.dto.NotificationCursorResponseDTO;
import org.lxdproject.lxd.notification.dto.NotificationPublishEvent;
import org.lxdproject.lxd.notification.dto.NotificationRequestDTO;
import org.lxdproject.lxd.notification.dto.NotificationResponseDTO;
import org.lxdproject.lxd.notification.entity.Notification;
import org.lxdproject.lxd.notification.message.NotificationMessageResolverManager;
import org.lxdproject.lxd.notification.publisher.NotificationPublisher;
import org.lxdproject.lxd.notification.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationPublisher notificationPublisher;
    private final MemberRepository memberRepository;
    private final NotificationMessageResolverManager messageResolverManager;

    public void saveAndPublishNotification(NotificationRequestDTO dto) {

        Member receiver = memberRepository.findById(dto.getReceiverId())
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        Member sender = memberRepository.findById(SecurityUtil.getCurrentMemberId())
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        String message = messageResolverManager.resolve(dto, sender, sender.getNativeLanguage().toLocale());

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
    }

    public NotificationCursorResponseDTO getNotifications(Boolean isRead, Long lastId, int size) {
        Long memberId = SecurityUtil.getCurrentMemberId();
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        List<NotificationResponseDTO> content = notificationRepository.findNotificationsWithCursor(memberId, isRead, lastId, size);

        boolean hasNext = content.size() == size;
        Long nextCursor = hasNext ? content.get(size - 1).getId() : null;

        return new NotificationCursorResponseDTO(content, nextCursor, hasNext);
    }
}

