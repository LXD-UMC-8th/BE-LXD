package org.lxdproject.lxd.global.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties("jwt")
public class JwtProperties {
    private String secret="";

    private AccessToken accessToken;
    private RefreshToken refreshToken;

    @Getter
    @Setter
    public static class AccessToken {
        private Long expiration;
    }

    @Getter
    @Setter
    public static class RefreshToken {
        private Long expiration;
    }

}
