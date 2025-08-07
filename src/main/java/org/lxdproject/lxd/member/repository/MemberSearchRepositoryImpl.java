package org.lxdproject.lxd.member.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.friend.dto.FriendSearchResponseDTO;
import org.lxdproject.lxd.member.entity.QMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class MemberSearchRepositoryImpl implements MemberRepositoryCustom{
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<FriendSearchResponseDTO.MemberInfo> searchByQuery(
            String query,
            Long currentUserId,
            Set<Long> friendIds,
            Pageable pageable) {

        QMember m = QMember.member;

        // 검색 조건
        BooleanExpression keywordMatch = m.username.containsIgnoreCase(query)
                .or(m.nickname.containsIgnoreCase(query));

        BooleanExpression excludeCurrentUser = m.id.ne(currentUserId);

        BooleanExpression notDeleted = m.deletedAt.isNull();

        // 친구인 사람 먼저, 닉네임 가나다 순으로 정렬
        OrderSpecifier<Integer> isFriendDesc = new CaseBuilder()
                .when(m.id.in(friendIds)).then(1)
                .otherwise(0)
                .desc();

        OrderSpecifier<String> nicknameAsc = m.nickname.asc();

        List<FriendSearchResponseDTO.MemberInfo> content = queryFactory
                .select(Projections.constructor(
                        FriendSearchResponseDTO.MemberInfo.class,
                        m.id,
                        m.username,
                        m.nickname,
                        m.profileImg
                ))
                .from(m)
                .where(keywordMatch, excludeCurrentUser, notDeleted)
                .orderBy(isFriendDesc, nicknameAsc)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .select(m.count())
                .from(m)
                .where(keywordMatch, excludeCurrentUser, notDeleted)
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }
}
