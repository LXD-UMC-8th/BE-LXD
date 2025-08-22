package org.lxdproject.lxd.config.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "aws.s3")
@Validated
public class S3Properties {
    @NotBlank
    private String bucket;
}
