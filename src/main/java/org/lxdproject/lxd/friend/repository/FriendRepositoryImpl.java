package org.lxdproject.lxd.friend.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.authz.predicate.MemberPredicates;
import org.lxdproject.lxd.friend.entity.Friendship;
import org.lxdproject.lxd.friend.entity.QFriendship;
import org.lxdproject.lxd.member.entity.Member;
import org.lxdproject.lxd.member.entity.QMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@RequiredArgsConstructor
public class FriendRepositoryImpl implements FriendRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final EntityManager em;
    private final MemberPredicates memberPredicates;

    private static final QFriendship friendship = QFriendship.friendship;
    private static final QMember MEMBER = QMember.member;

    // create, delete, update 양방향, read 단방향
    // 1. 친구 관계 조회 <read>  → 단방향 조회
    @Override
    public Page<Member> findFriendsByMemberId(Long memberId, Pageable pageable) { // 친구 목록 반환
        BooleanExpression condition = friendship.requester.id.eq(memberId)
                .and(friendship.deletedAt.isNull())
                .and(memberPredicates.isNotDeleted(MEMBER));

        List<Member> result = queryFactory
                .select(friendship.receiver)
                .from(friendship)
                .join(friendship.receiver, MEMBER)
                .where(condition)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = Optional.ofNullable(queryFactory
                .select(Wildcard.count)
                .from(friendship)
                .join(friendship.receiver, MEMBER)
                .where(condition)
                .fetchOne()).orElse(0L);

        return new PageImpl<>(result, pageable, total);
    }

    // 3. 친구 관계 삭제 <delete> → 양방향 삭제
    @Transactional
    public void deleteFriendship(Member m1, Member m2) {
        queryFactory
                .delete(friendship)
                .where(
                        (friendship.requester.id.eq(m1.getId()).and(friendship.receiver.id.eq(m2.getId())))
                                .or(friendship.requester.id.eq(m2.getId()).and(friendship.receiver.id.eq(m1.getId())))
                )
                .execute();
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
    public boolean areFriends(Long requesterId, Long receiverId) {
        return queryFactory
                .selectOne()
                .from(friendship)
                .where(
                        friendship.requester.id.eq(requesterId)
                                .and(friendship.receiver.id.eq(receiverId))
                                .and(friendship.deletedAt.isNull())
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
        return Optional.ofNullable(queryFactory
                .select(Wildcard.count)
                .from(friendship)
                .join(friendship.receiver, MEMBER)
                .where(
                        friendship.requester.id.eq(memberId),
                        friendship.deletedAt.isNull(),
                        memberPredicates.isNotDeleted(MEMBER)
                )
                .fetchOne()).orElse(0L);
    }

}
