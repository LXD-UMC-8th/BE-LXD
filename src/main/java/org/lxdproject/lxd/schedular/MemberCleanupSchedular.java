package org.lxdproject.lxd.schedular;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lxdproject.lxd.member.service.MemberService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class MemberCleanupSchedular {

    private final MemberService memberService;

    // 매일 자정에 실행 (cron = "초 분 시 일 월 요일")
    @Scheduled(cron = "0 0 0 * * ?", zone = "Asia/Seoul")
    public void cleanupWithdrawnMembers() {
        // 일기, 일기 댓글에 대해 hardDelete 수행
        log.info("탈퇴한 지 30일이 지난 회원 스케쥴링 중 ...");
        memberService.hardDeleteWithdrawnMembers();

    }

}
