package org.lxdproject.lxd.global.discord;

import io.sentry.Sentry;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Discord WebHook API [TEMP]", description = "Discord Webhook 연동 테스트 관련 API 입니다. [TEMP]")
@Profile("dev")
public class DiscordTestController {
    @GetMapping("/discord-test/throw")
    public void throwErr() {
        throw new RuntimeException("디스코드 연동 테스트 성공");
    }
}