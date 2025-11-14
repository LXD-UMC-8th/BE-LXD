package org.lxdproject.lxd.global.authz.guard;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.global.apiPayload.code.exception.handler.MemberHandler;
import org.lxdproject.lxd.global.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.global.authz.model.Permit;
import org.lxdproject.lxd.global.authz.policy.MemberPolicy;
import org.lxdproject.lxd.domain.member.entity.Member;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberGuard {
    private final MemberPolicy memberPolicy;

    public void checkOwnerIsNotDeleted(Member member) {
        if (memberPolicy.checkDeletedMember(member) == Permit.WITHDRAWN) {
            throw new MemberHandler(ErrorStatus.RESOURCE_OWNER_WITHDRAWN);
        }
    }

}
