package org.lxdproject.lxd.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "aws")
@Component
@Getter @Setter
@Validated
public class AwsProperties {

    private String region;
    private final Credentials credentials = new Credentials();

    @Getter @Setter
    public static class Credentials {
        private String accessKey;
        private String secretKey;
    }
}
