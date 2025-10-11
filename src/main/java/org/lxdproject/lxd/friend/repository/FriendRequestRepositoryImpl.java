package org.lxdproject.lxd.friend.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.authz.predicate.MemberPredicates;
import org.lxdproject.lxd.friend.dto.FriendResponseDTO;
import org.lxdproject.lxd.friend.entity.QFriendRequest;
import org.lxdproject.lxd.friend.entity.enums.FriendRequestStatus;
import org.lxdproject.lxd.member.entity.Member;
import org.lxdproject.lxd.member.entity.QMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class FriendRequestRepositoryImpl implements FriendRequestRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    private final MemberPredicates memberPredicates;

    private static final QFriendRequest FR = QFriendRequest.friendRequest;
    private static final QMember M = QMember.member;

    @Override
    public Page<FriendResponseDTO> findReceivedRequestDTOs(Member receiver, FriendRequestStatus status, Pageable pageable) {
        BooleanExpression condition = FR.receiver.eq(receiver)
                .and(FR.status.eq(status))
                .and(memberPredicates.isNotDeleted(M));

        List<FriendResponseDTO> content = queryFactory
                .select(Projections.constructor(FriendResponseDTO.class,
                        M.id, M.username, M.nickname, M.profileImg))
                .from(FR)
                .join(FR.requester, M)
                .where(condition)
                .orderBy(FR.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = Optional.ofNullable(queryFactory
                .select(Wildcard.count)
                .from(FR)
                .join(FR.requester, M)
                .where(condition)
                .fetchOne()).orElse(0L);

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<FriendResponseDTO> findSentRequestDTOs(Member requester, FriendRequestStatus status, Pageable pageable) {
        BooleanExpression condition = FR.requester.eq(requester)
                .and(FR.status.eq(status))
                .and(memberPredicates.isNotDeleted(M));

        List<FriendResponseDTO> content = queryFactory
                .select(Projections.constructor(FriendResponseDTO.class,
                        M.id, M.username, M.nickname, M.profileImg))
                .from(FR)
                .join(FR.receiver, M)
                .where(condition)
                .orderBy(FR.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = Optional.ofNullable(queryFactory
                .select(Wildcard.count)
                .from(FR)
                .join(FR.receiver, M)
                .where(condition)
                .fetchOne()).orElse(0L);

        return new PageImpl<>(content, pageable, total);
    }
}
