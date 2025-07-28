package org.lxdproject.lxd.member.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.member.entity.Member;
import org.lxdproject.lxd.member.entity.QMember;
import org.lxdproject.lxd.member.entity.QFriendship;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class FriendRepositoryImpl implements FriendRepository {

    private final JPAQueryFactory queryFactory;

    QFriendship friendship = QFriendship.friendship;
    QMember requester = new QMember("requester");
    QMember receiver = new QMember("receiver");


    @Override
    public List<Member> findFriendsByMemberId(Long memberId) {
        // 내가 요청자였던 경우: receiver가 친구
        List<Member> sent = queryFactory
                .select(friendship.receiver)
                .from(friendship)
                .where(friendship.requester.id.eq(memberId))
                .fetch();

        // 내가 수락자였던 경우: requester가 친구
        List<Member> received = queryFactory
                .select(friendship.requester)
                .from(friendship)
                .where(friendship.receiver.id.eq(memberId))
                .fetch();

        // 두 리스트 합치기
        sent.addAll(received);
        return sent;
    }
}
