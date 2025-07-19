package org.lxdproject.lxd.config.properties;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "urls")
@Component
@Getter
@Setter
public class UrlProperties {
    private String frontend;
    private String backend;
}
