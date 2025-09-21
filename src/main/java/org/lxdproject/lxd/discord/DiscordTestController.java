package org.lxdproject.lxd.discord;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Discord WebHook API [TEMP]", description = "Discord Webhook 연동 테스트 관련 API 입니다. [TEMP]")
public class DiscordTestController {
    @GetMapping("/discord-test/throw")
    public String throwErr() {
        throw new RuntimeException("디스코드 연동 테스트 성공");
    }
}