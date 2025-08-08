package org.lxdproject.lxd.config.properties;


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
    // 순서 계약: [local, deployed]
    @jakarta.validation.constraints.NotEmpty
    private List<@jakarta.validation.constraints.NotBlank String> frontend;
    @jakarta.validation.constraints.NotEmpty
    private List<@jakarta.validation.constraints.NotBlank String> backend;
}
