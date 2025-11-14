package org.lxdproject.lxd.global.config.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "aws")
@Component
@Getter @Setter
public class AwsProperties {

    @NotBlank
    private String region;
    @NestedConfigurationProperty
    private final Credentials credentials = new Credentials();

    @Getter @Setter
    public static class Credentials {
        @NotBlank
        private String accessKey;
        @NotBlank
        private String secretKey;
    }
}
