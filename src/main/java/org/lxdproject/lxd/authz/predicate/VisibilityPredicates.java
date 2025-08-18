package org.lxdproject.lxd.authz.predicate;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import lombok.experimental.UtilityClass;
import org.lxdproject.lxd.diary.entity.QDiary;
import org.lxdproject.lxd.diary.entity.enums.Visibility;

import java.util.Set;

@UtilityClass
public class VisibilityPredicates {
    public static BooleanExpression diaryVisibleTo(Long viewerId, QDiary D, Set<Long> friendIds) {
        BooleanExpression isPublic = D.visibility.eq(Visibility.PUBLIC);
        BooleanExpression isMine   = viewerId == null ? Expressions.FALSE : D.member.id.eq(viewerId);
        BooleanExpression isFriends = (viewerId != null && friendIds != null && !friendIds.isEmpty())
                ? D.visibility.eq(Visibility.FRIENDS).and(D.member.id.in(friendIds))
                : Expressions.FALSE;
        return isPublic.or(isFriends).or(isMine);
    }
}
