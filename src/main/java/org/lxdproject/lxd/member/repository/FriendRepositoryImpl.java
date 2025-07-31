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

import java.util.List;

@Repository
@RequiredArgsConstructor
@Transactional
public class FriendRepositoryImpl implements FriendRepository {

    private final JPAQueryFactory queryFactory;
    private final EntityManager em;

    QFriendship friendship = QFriendship.friendship;
    QMember requester = new QMember("requester");
    QMember receiver = new QMember("receiver");


    @Override
    public List<Member> findFriendsByMemberId(Long memberId) {
        // 내가 요청자였던 경우: receiver가 친구
        List<Member> sent = queryFactory
                .select(friendship.receiver)
                .from(friendship)
                .where(
                        friendship.requester.id.eq(memberId),
                        friendship.deletedAt.isNull()
                )
                .fetch();

        // 내가 수락자였던 경우: requester가 친구
        List<Member> received = queryFactory
                .select(friendship.requester)
                .from(friendship)
                .where(
                        friendship.receiver.id.eq(memberId),
                        friendship.deletedAt.isNull()
                )
                .fetch();

        // 두 리스트 합치기
        sent.addAll(received);
        return sent;
    }

    // 양방향 조회
    @Override
    public boolean existsByRequesterAndReceiverOrReceiverAndRequester(Member m1, Member m2) {
        return queryFactory
                .selectOne()
                .from(friendship)
                .where(
                        (friendship.requester.eq(m1).and(friendship.receiver.eq(m2)))
                                .or(friendship.requester.eq(m2).and(friendship.receiver.eq(m1))),
                        friendship.deletedAt.isNull() // 삭제된 관계는 포함하지 않고 조회하도록 수정
                )
                .fetchFirst() != null;
    }

    @Override
    public void saveFriendship(Member requester, Member receiver) {
        Friendship existing = findFriendshipIncludingDeleted(requester, receiver);
        if (existing != null) {
            if (existing.isDeleted()) {
                existing.restore(); // 복원
            }
        } else {
            em.persist(Friendship.builder()
                    .requester(requester)
                    .receiver(receiver)
                    .build());
        }

        // 양방향 구현
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

    private Friendship findFriendshipIncludingDeleted(Member requester, Member receiver) {
        return queryFactory
                .selectFrom(friendship)
                .where(
                        friendship.requester.eq(requester),
                        friendship.receiver.eq(receiver)
                )
                .fetchOne();
    }

    @Override
    public void softDeleteFriendship(Member m1, Member m2) {
        Friendship forward = findFriendshipIncludingDeleted(m1, m2);
        if (forward != null && !forward.isDeleted()) {
            forward.softDelete();
        }

        Friendship reverse = findFriendshipIncludingDeleted(m2, m1);
        if (reverse != null && !reverse.isDeleted()) {
            reverse.softDelete();
        }
    }

    @Override
    public boolean existsFriendRelation(Long memberId, Long friendId) {
        return queryFactory
                .selectOne()
                .from(friendship)
                .where(
                        (
                                friendship.requester.id.eq(memberId)
                                        .and(friendship.receiver.id.eq(friendId))
                        ).or(
                                friendship.requester.id.eq(friendId)
                                        .and(friendship.receiver.id.eq(memberId))
                        ),
                        friendship.deletedAt.isNull()
                )
                .fetchFirst() != null;
    }
}
