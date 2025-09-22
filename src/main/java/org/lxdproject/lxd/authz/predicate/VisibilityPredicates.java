package org.lxdproject.lxd.authz.predicate;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import org.lxdproject.lxd.diary.entity.QDiary;
import org.lxdproject.lxd.diary.entity.enums.Visibility;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class VisibilityPredicates {
    public static BooleanExpression diaryVisibleTo(Long viewerId, QDiary D, Set<Long> friendIds) {
        BooleanExpression isPublic = D.visibility.eq(Visibility.PUBLIC);
        BooleanExpression isMine   = viewerId == null ? Expressions.FALSE : D.member.id.eq(viewerId);
        BooleanExpression isFriends = (viewerId != null && friendIds != null && !friendIds.isEmpty())
                ? D.visibility.eq(Visibility.FRIENDS).and(D.member.id.in(friendIds))
                : Expressions.FALSE;
        BooleanExpression isAuthorNotDeleted = D.member.isNotNull().and(D.member.deletedAt.isNull());
        return isPublic.or(isFriends).or(isMine).or(isAuthorNotDeleted);
    }

    // 내 글 제외
    public static BooleanExpression diaryVisibleToOthers(Long viewerId, QDiary D, Set<Long> friendIds) {
        BooleanExpression visible = diaryVisibleTo(viewerId, D, friendIds);
        return viewerId == null ? visible : visible.and(D.member.id.ne(viewerId));
    }
}
