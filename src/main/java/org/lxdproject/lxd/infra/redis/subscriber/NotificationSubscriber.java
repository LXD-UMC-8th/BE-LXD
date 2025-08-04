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
import org.lxdproject.lxd.notification.message.NotificationMessageResolverManager;
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
    private final MemberRepository memberRepository;
    private final NotificationMessageResolverManager messageResolverManager;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            // Redis 메시지 수신
            String body = new String(message.getBody());
            NotificationMessageContext dto = objectMapper.readValue(body, NotificationMessageContext.class);

            log.debug("[RedisSubscriber] 메시지 수신: {}", dto);

            // 클라이언트용 메시지 생성
            Notification notification = notificationRepository.findWithSenderAndReceiverById(dto.getNotificationId())
                    .orElseThrow(() -> new NotificationHandler(ErrorStatus.NOTIFICATION_NOT_FOUND));

            Member sender = notification.getSender();
            Member receiver = notification.getReceiver();

            // 알림 받는 사람의 언어를 기준으로 메시지 생성
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

        } catch (Exception e) {
            log.error("[RedisSubscriber] 메시지 처리 실패", e);
        }
    }
}