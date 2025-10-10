package org.lxdproject.lxd.authz.policy;

import org.lxdproject.lxd.authz.model.Permit;
import org.lxdproject.lxd.member.entity.Member;
import org.springframework.stereotype.Component;

@Component
public class MemberPolicy {
    public Permit checkDeletedMember(Member member) {
        if (member == null || member.isDeleted()) {
            return Permit.WITHDRAWN; // 탈퇴 회원
        }
        return Permit.ALLOW; // 정상 회원
    }
}
