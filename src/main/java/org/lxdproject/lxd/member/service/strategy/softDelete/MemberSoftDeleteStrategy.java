package org.lxdproject.lxd.member.service.strategy.softDelete;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.code.exception.handler.MemberHandler;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.member.entity.Member;
import org.lxdproject.lxd.member.repository.MemberRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class MemberSoftDeleteStrategy implements SoftDeleteStrategy {

    private final MemberRepository memberRepository;

    @Override
    public void softDelete(Long memberId, LocalDateTime deletedAt) {

        // 멤버 soft delete
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));

        member.softDelete(deletedAt);

    }
}
