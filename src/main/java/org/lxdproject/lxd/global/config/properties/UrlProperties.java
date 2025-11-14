package org.lxdproject.lxd.global.config.properties;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@ConfigurationProperties(prefix = "urls")
@Component
@Getter
@Setter
@Validated
public class UrlProperties {
    private String frontend;
    private String backend;
}
