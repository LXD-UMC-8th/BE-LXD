package org.lxdproject.lxd.friend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.lxdproject.lxd.common.entity.BaseEntity;
import org.lxdproject.lxd.member.entity.Member;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "friendship", indexes = {
        @Index(name = "idx_friend_requester", columnList = "requester_id"),
        @Index(name = "idx_friend_receiver", columnList = "receiver_id")
})
public class Friendship extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 친구 요청을 보낸 사람 (관계 생성 시점 기준)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private Member requester;

    // 친구 수락한 사람
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private Member receiver;
}

