package org.lxdproject.lxd.member.strategy.hardDelete;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.member.repository.MemberRepository;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Order(HardDeleteOrder.MEMBER)
public class MemberHardDeleteStrategy implements HardDeleteStrategy {

    private final MemberRepository memberRepository;

    @Override
    public void hardDelete(LocalDateTime threshold) {

        // 30일이 지난 회원의 isPurged 값을 true로 만들고
        // 새로운 유저의 nickname/email의 unique 조건을 피하기 위해 대체값으로 치환
        memberRepository.hardDeleteMembersOlderThanThreshold(threshold);
    }
}
