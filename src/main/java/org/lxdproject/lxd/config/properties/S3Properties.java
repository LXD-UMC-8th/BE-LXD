package org.lxdproject.lxd.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "aws.s3")
public class S3Properties {
    private String bucket;
    private String defaultProfileUrl;
}
