package org.lxdproject.lxd.domain.member.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.domain.friend.dto.FriendSearchResponseDTO;
import org.lxdproject.lxd.member.entity.QMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;


@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom{
    private final JPAQueryFactory queryFactory;
    private static final QMember member = new QMember("member");

    @Override
    public Page<FriendSearchResponseDTO.MemberInfo> searchByQuery(
            String keyword,
            Long currentMemberId,
            Set<Long> friendIdSet,
            Pageable pageable
    ) {
        BooleanExpression notCurrentUser = member.id.ne(currentMemberId);
        BooleanExpression notDeleted = member.deletedAt.isNull();

        // username 또는 nickname LIKE 검색
        BooleanExpression usernameLike = member.username.lower().like("%" + keyword.toLowerCase() + "%");
        BooleanExpression nicknameLike = member.nickname.lower().like("%" + keyword.toLowerCase() + "%");
        BooleanExpression searchCondition = usernameLike.or(nicknameLike);

        // 친구 여부에 따라 정렬 조건 추가
        NumberExpression<Integer> friendPriority = new CaseBuilder()
                .when(member.id.in(friendIdSet.isEmpty() ? List.of(-1L) : friendIdSet))
                .then(1)
                .otherwise(0);

        List<FriendSearchResponseDTO.MemberInfo> content = queryFactory
                .select(Projections.constructor(
                        FriendSearchResponseDTO.MemberInfo.class,
                        member.id,
                        member.username,
                        member.nickname,
                        member.profileImg
                ))
                .from(member)
                .where(
                        notCurrentUser,
                        notDeleted,
                        searchCondition
                )
                .orderBy(friendPriority.desc(), member.nickname.asc()) // 친구 우선, 닉네임 알파벳순
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .select(member.count())
                .from(member)
                .where(
                        notCurrentUser,
                        notDeleted,
                        searchCondition
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

}
