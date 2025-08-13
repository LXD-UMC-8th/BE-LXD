package org.lxdproject.lxd.config.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "aws")
@Component
@Getter
public class AwsProperties {

    @NotBlank
    private String region;
    @NestedConfigurationProperty
    private final Credentials credentials = new Credentials();

    @Getter
    public static class Credentials {
        @NotBlank
        private String accessKey;
        @NotBlank
        private String secretKey;
    }
}
