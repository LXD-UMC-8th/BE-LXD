package org.lxdproject.lxd.domain.friend.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.domain.friend.entity.QFriendship;
import org.lxdproject.lxd.domain.member.entity.QMember;
import org.lxdproject.lxd.global.authz.predicate.MemberPredicates;
import org.lxdproject.lxd.domain.friend.entity.Friendship;
import org.lxdproject.lxd.domain.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@RequiredArgsConstructor
public class FriendRepositoryImpl implements FriendRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final EntityManager entityManager;
    private final MemberPredicates memberPredicates;

    private static final QFriendship FRIENDSHIP = QFriendship.friendship;
    private static final QMember MEMBER = QMember.member;

    // create, delete, update 양방향, read 단방향
    // 1. 친구 관계 조회 <read>  → 단방향 조회
    @Override
    public Page<Member> findFriendsByMemberId(Long memberId, Pageable pageable) { // 친구 목록 반환
        BooleanExpression condition = FRIENDSHIP.requester.id.eq(memberId)
                .and(FRIENDSHIP.deletedAt.isNull())
                .and(memberPredicates.isNotDeleted(MEMBER));

        List<Member> result = queryFactory
                .select(FRIENDSHIP.receiver)
                .from(FRIENDSHIP)
                .join(FRIENDSHIP.receiver, MEMBER)
                .where(condition)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = Optional.ofNullable(queryFactory
                .select(Wildcard.count)
                .from(FRIENDSHIP)
                .join(FRIENDSHIP.receiver, MEMBER)
                .where(condition)
                .fetchOne()).orElse(0L);

        return new PageImpl<>(result, pageable, total);
    }

    // 3. 친구 관계 삭제 <delete> → 양방향 삭제
    @Transactional
    public void deleteFriendship(Member m1, Member m2) {
        queryFactory
                .delete(FRIENDSHIP)
                .where(
                        (FRIENDSHIP.requester.id.eq(m1.getId()).and(FRIENDSHIP.receiver.id.eq(m2.getId())))
                                .or(FRIENDSHIP.requester.id.eq(m2.getId()).and(FRIENDSHIP.receiver.id.eq(m1.getId())))
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
            entityManager.persist(Friendship.builder()
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
            entityManager.persist(Friendship.builder()
                    .requester(receiver)
                    .receiver(requester)
                    .build());
        }
    }

    // 5. 친구 관계 조회 (삭제된 것도 포함) <read> → 단방향 조회
    private Friendship findFriendshipIncludingDeleted(Member requester, Member receiver) {
        return queryFactory
                .selectFrom(FRIENDSHIP)
                .where(
                        FRIENDSHIP.requester.eq(requester),
                        FRIENDSHIP.receiver.eq(receiver)
                )
                .fetchOne();
    }

    // 6. 친구 관계 조회 (Friendship 객체로 조회) <read> → 단방향 조회
    private Friendship findFriendshipEntity(Long requesterId, Long receiverId) {
        return queryFactory
                .selectFrom(FRIENDSHIP)
                .where(
                        FRIENDSHIP.requester.id.eq(requesterId),
                        FRIENDSHIP.receiver.id.eq(receiverId),
                        FRIENDSHIP.deletedAt.isNull()
                )
                .fetchOne();
    }

    // 7. 친구 관계 조회 <read> → 단방향 조회
    @Override
    public boolean areFriends(Long requesterId, Long receiverId) {
        return queryFactory
                .selectOne()
                .from(FRIENDSHIP)
                .where(
                        FRIENDSHIP.requester.id.eq(requesterId)
                                .and(FRIENDSHIP.receiver.id.eq(receiverId))
                                .and(FRIENDSHIP.deletedAt.isNull())
                )
                .fetchFirst() != null;
    }

    @Override
    public Set<Long> findFriendIdsByMemberId(Long memberId) {
        List<Long> sentFriendIds = queryFactory
                .select(FRIENDSHIP.receiver.id)
                .from(FRIENDSHIP)
                .where(FRIENDSHIP.requester.id.eq(memberId),
                        FRIENDSHIP.deletedAt.isNull())
                .fetch();

        List<Long> receivedFriendIds = queryFactory
                .select(FRIENDSHIP.requester.id)
                .from(FRIENDSHIP)
                .where(FRIENDSHIP.receiver.id.eq(memberId),
                        FRIENDSHIP.deletedAt.isNull())
                .fetch();

        Set<Long> friendIds = new HashSet<>();
        friendIds.addAll(sentFriendIds);
        friendIds.addAll(receivedFriendIds);

        return friendIds;
    }

    @Override
    public void hardDeleteFriendshipsOlderThanThreshold(LocalDateTime threshold) {
        // 탈퇴 후 아직 purged 전인 회원 조회
        List<Long> withdrawnMemberIds = queryFactory
                .select(MEMBER.id)
                .from(MEMBER)
                .where(MEMBER.deletedAt.isNotNull()
                        .and(MEMBER.deletedAt.loe(threshold))
                        .and(MEMBER.isPurged.isFalse()))
                .fetch();

        if (withdrawnMemberIds.isEmpty()) return;

        entityManager.flush();

        // 탈퇴 멤버가 포함된 친구 관계 전체 삭제
        queryFactory
                .delete(FRIENDSHIP)
                .where(
                        FRIENDSHIP.requester.id.in(withdrawnMemberIds)
                                .or(FRIENDSHIP.receiver.id.in(withdrawnMemberIds))
                )
                .execute();

        entityManager.clear();
    }

    @Override
    @Transactional
    public long countFriendsByMemberId(Long memberId) {
        return Optional.ofNullable(queryFactory
                .select(Wildcard.count)
                .from(FRIENDSHIP)
                .join(FRIENDSHIP.receiver, MEMBER)
                .where(
                        FRIENDSHIP.requester.id.eq(memberId),
                        FRIENDSHIP.deletedAt.isNull(),
                        memberPredicates.isNotDeleted(MEMBER)
                )
                .fetchOne()).orElse(0L);
    }

}
