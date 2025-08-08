package org.lxdproject.lxd.config.properties;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@ConfigurationProperties(prefix = "urls")
@Component
@Getter
@Setter
public class UrlProperties {
    private List<String> frontend;
    private List<String> backend;
}
