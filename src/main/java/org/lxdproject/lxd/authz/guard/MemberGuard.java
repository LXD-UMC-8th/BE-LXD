package org.lxdproject.lxd.authz.guard;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.code.exception.handler.MemberHandler;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.authz.model.Permit;
import org.lxdproject.lxd.authz.policy.MemberPolicy;
import org.lxdproject.lxd.member.entity.Member;
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
