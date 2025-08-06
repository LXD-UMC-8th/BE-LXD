package org.lxdproject.lxd.member.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.member.entity.Friendship;
import org.lxdproject.lxd.member.entity.Member;
import org.lxdproject.lxd.member.entity.QMember;
import org.lxdproject.lxd.member.entity.QFriendship;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Transactional
public class FriendRepositoryImpl implements FriendRepository {

    private final JPAQueryFactory queryFactory;
    private final EntityManager em;

    private static final QFriendship friendship = QFriendship.friendship;
    private static final QMember requester = new QMember("requester");
    private static final QMember receiver = new QMember("receiver");

    // create, delete, update 양방향, read 단방향
    // 1. 친구 관계 조회 <read>  → 단방향 조회
    @Override
    public List<Member> findFriendsByMemberId(Long memberId) { // 친구 목록 반환
        List<Member> friends = new ArrayList<>();
        friends.addAll(queryFactory
                .select(friendship.receiver)
                .from(friendship)
                .where(
                        friendship.requester.id.eq(memberId),
                        friendship.deletedAt.isNull()
                )
                .fetch());
        return friends;
    }

    // 2. 친구 여부 반환 <read>  → 단방향 조회
    @Override
    public boolean existsFriendshipByRequesterAndReceiver(Member m1, Member m2) {
        return queryFactory
                .selectOne()
                .from(friendship)
                .where(
                        (friendship.requester.eq(m1).and(friendship.receiver.eq(m2))),
                        friendship.deletedAt.isNull()
                )
                .fetchFirst() != null;
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
    public boolean existsFriendRelation(Long memberId, Long friendId) {
        return queryFactory
                .selectOne()
                .from(friendship)
                .where(
                        (
                                friendship.requester.id.eq(memberId)
                                        .and(friendship.receiver.id.eq(friendId))
                        ),
                        friendship.deletedAt.isNull()
                )
                .fetchFirst() != null;
    }
}
