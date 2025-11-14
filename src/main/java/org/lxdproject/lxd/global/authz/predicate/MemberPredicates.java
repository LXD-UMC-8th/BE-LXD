package org.lxdproject.lxd.global.authz.predicate;

import com.querydsl.core.types.dsl.BooleanExpression;
import org.lxdproject.lxd.member.entity.QMember;
import org.springframework.stereotype.Component;

@Component
public class MemberPredicates {

    public BooleanExpression isNotDeleted(QMember member) {
        return member.isNotNull().and(member.deletedAt.isNull());
    }

    public BooleanExpression isDeleted(QMember member) {
        return member.deletedAt.isNotNull();
    }
}
