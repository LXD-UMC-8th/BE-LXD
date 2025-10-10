package org.lxdproject.lxd.authz.predicate;

import com.querydsl.core.types.dsl.BooleanExpression;
import org.lxdproject.lxd.member.entity.QMember;
import org.springframework.stereotype.Component;

@Component
public class MemberPredicates {

    public static BooleanExpression isNotDeleted(QMember member) {
        return member.isNotNull().and(member.deletedAt.isNull());
    }

    public static BooleanExpression isDeleted(QMember member) {
        return member.deletedAt.isNotNull();
    }
}
