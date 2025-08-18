package org.lxdproject.lxd.friend.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.friend.entity.Friendship;
import org.lxdproject.lxd.friend.entity.QFriendship;
import org.lxdproject.lxd.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class FriendRepositoryImpl implements FriendRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final EntityManager em;

    private static final QFriendship friendship = QFriendship.friendship;

    // create, delete, update 양방향, read 단방향
    // 1. 친구 관계 조회 <read>  → 단방향 조회
    @Override
    public Page<Member> findFriendsByMemberId(Long memberId, Pageable pageable) { // 친구 목록 반환
        List<Member> result = new ArrayList<>();

        result.addAll(queryFactory
                .select(friendship.receiver)
                .from(friendship)
                .where(
                        friendship.requester.id.eq(memberId),
                        friendship.deletedAt.isNull()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch());

        long total = queryFactory
                .select(friendship.count())
                .from(friendship)
                .where(
                        friendship.requester.id.eq(memberId),
                        friendship.deletedAt.isNull()
                )
                .fetchOne();

        return new PageImpl<>(result, pageable, total);
    }

    // 3. 친구 관계 삭제 <delete> → 양방향 삭제
    @Override
    public void softDeleteFriendship(Member m1, Member m2) {
        Long m1Id = m1.getId();
        Long m2Id = m2.getId();

        Friendship forward = findFriendshipEntity(m1Id, m2Id);
        if (forward != null && !forward.isDeleted()) {
            forward.softDelete();
        }

        Friendship reverse = findFriendshipEntity(m2Id, m1Id);
        if (reverse != null && !reverse.isDeleted()) {
            reverse.softDelete();
        }
    }

   // 4. 친구 관계 저장 <create> → 양방향 저장
    @Override
    public void saveFriendship(Member requester, Member receiver) {
        Friendship existing = findFriendshipIncludingDeleted(requester, receiver);
        if (existing != null) {
            if (existing.isDeleted()) {
                existing.restore();
            }
        } else {
            em.persist(Friendship.builder()
                    .requester(requester)
                    .receiver(receiver)
                    .build());
        }

        Friendship reverse = findFriendshipIncludingDeleted(receiver, requester);
        if (reverse != null) {
            if (reverse.isDeleted()) {
                reverse.restore();
            }
        } else {
            em.persist(Friendship.builder()
                    .requester(receiver)
                    .receiver(requester)
                    .build());
        }
    }

    // 5. 친구 관계 조회 (삭제된 것도 포함) <read> → 단방향 조회
    private Friendship findFriendshipIncludingDeleted(Member requester, Member receiver) {
        return queryFactory
                .selectFrom(friendship)
                .where(
                        friendship.requester.eq(requester),
                        friendship.receiver.eq(receiver)
                )
                .fetchOne();
    }

    // 6. 친구 관계 조회 (Friendship 객체로 조회) <read> → 단방향 조회
    private Friendship findFriendshipEntity(Long requesterId, Long receiverId) {
        return queryFactory
                .selectFrom(friendship)
                .where(
                        friendship.requester.id.eq(requesterId),
                        friendship.receiver.id.eq(receiverId),
                        friendship.deletedAt.isNull()
                )
                .fetchOne();
    }

    // 7. 친구 관계 조회 <read> → 단방향 조회
    @Override
    public boolean areFriends(Long memberId1, Long memberId2) {
        return queryFactory
                .selectOne()
                .from(friendship)
                .where(
                        (
                                friendship.requester.id.eq(memberId1).and(friendship.receiver.id.eq(memberId2))
                                        .or(friendship.requester.id.eq(memberId2).and(friendship.receiver.id.eq(memberId1)))
                        ).and(friendship.deletedAt.isNull())
                )
                .fetchFirst() != null;
    }

    @Override
    public Set<Long> findFriendIdsByMemberId(Long memberId) {
        List<Long> sentFriendIds = queryFactory
                .select(friendship.receiver.id)
                .from(friendship)
                .where(friendship.requester.id.eq(memberId),
                        friendship.deletedAt.isNull())
                .fetch();

        List<Long> receivedFriendIds = queryFactory
                .select(friendship.requester.id)
                .from(friendship)
                .where(friendship.receiver.id.eq(memberId),
                        friendship.deletedAt.isNull())
                .fetch();

        Set<Long> friendIds = new HashSet<>();
        friendIds.addAll(sentFriendIds);
        friendIds.addAll(receivedFriendIds);

        return friendIds;
    }

    @Override
    @Transactional
    public long countFriendsByMemberId(Long memberId) {
        return queryFactory
                .select(friendship.count())
                .from(friendship)
                .where(
                        friendship.requester.id.eq(memberId),
                        friendship.deletedAt.isNull()
                )
                .fetchOne();
    }

}
