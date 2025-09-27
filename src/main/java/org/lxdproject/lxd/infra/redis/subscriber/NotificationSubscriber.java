package org.lxdproject.lxd.infra.redis.subscriber;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lxdproject.lxd.apiPayload.code.exception.handler.NotificationHandler;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.common.util.DateFormatUtil;
import org.lxdproject.lxd.member.entity.Member;
import org.lxdproject.lxd.member.repository.MemberRepository;
import org.lxdproject.lxd.notification.dto.MessagePart;
import org.lxdproject.lxd.notification.dto.NotificationResponseDTO;
import org.lxdproject.lxd.notification.dto.NotificationMessageContext;
import org.lxdproject.lxd.notification.entity.Notification;
import org.lxdproject.lxd.notification.entity.enums.NotificationType;
import org.lxdproject.lxd.notification.message.MessageResolverManager;
import org.lxdproject.lxd.notification.repository.NotificationRepository;
import org.lxdproject.lxd.notification.service.SseEmitterService;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationSubscriber implements MessageListener {

    private final ObjectMapper objectMapper; // 메시지 역직렬화용
    private final SseEmitterService sseEmitterService;
    private final NotificationRepository notificationRepository;
    private final MessageResolverManager messageResolverManager;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            // Redis 메시지 수신
            String body = new String(message.getBody());
            NotificationMessageContext dto = objectMapper.readValue(body, NotificationMessageContext.class);

            log.debug("[RedisSubscriber] 메시지 수신: {}", dto);

            // 이벤트 타입에 따라 분기 처리
            switch (dto.getEventType()) {
                case CREATED -> handleCreated(dto);
                case DELETED -> handleDeleted(dto);
                case READ -> handleRead(dto);
                case ALL_READ -> handleAllRead(dto);
                default -> log.warn("[RedisSubscriber] 알 수 없는 이벤트 타입: {}", dto.getEventType());
            }

        } catch (Exception e) {
            log.error("[RedisSubscriber] 메시지 처리 실패", e);
        }
    }

    private void handleCreated(NotificationMessageContext dto) {
        notificationRepository.findWithSenderAndReceiverById(dto.getNotificationId())
                .ifPresentOrElse(notification -> {
                    Member sender = notification.getSender();
                    Member receiver = notification.getReceiver();

                    // 알림 받는 사람의 언어 기준 메시지 생성
                    Locale locale = receiver.getNativeLanguage().toLocale();
                    List<MessagePart> parts = messageResolverManager.resolve(dto, locale);

                    NotificationResponseDTO response = NotificationResponseDTO.builder()
                            .id(notification.getId())
                            .profileImg(sender.getProfileImg())
                            .messageParts(parts)
                            .redirectUrl(notification.getRedirectUrl())
                            .isRead(notification.isRead())
                            .buttonField(notification.getNotificationType() == NotificationType.FRIEND_REQUEST)
                            .createdAt(DateFormatUtil.formatDate(notification.getCreatedAt()))
                            .build();

                    // SSE 전송
                    sseEmitterService.send(dto.getReceiverId(), response);
                }, () -> {
                    new NotificationHandler(ErrorStatus.NOTIFICATION_NOT_FOUND);
                });
    }

    private void handleDeleted(NotificationMessageContext dto) {
        sseEmitterService.sendNotificationDeleted(
                dto.getReceiverId(),
                dto.getNotificationId(),
                dto.getNotificationType(),
                dto.getTargetType(),
                dto.getTargetId()
        );
    }

    private void handleRead(NotificationMessageContext dto) {
        sseEmitterService.sendNotificationReadUpdate(
                dto.getReceiverId(),
                dto.getNotificationId()
        );
    }

    private void handleAllRead(NotificationMessageContext dto) {
        sseEmitterService.sendAllReadUpdate(dto.getReceiverId());
    }
}