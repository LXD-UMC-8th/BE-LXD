package org.lxdproject.lxd.common.util;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class ProfileChecker {
    private final Environment env;

    public boolean isLocal() { return Arrays.asList(env.getActiveProfiles()).contains("local"); }
    public boolean isProd()  { return Arrays.asList(env.getActiveProfiles()).contains("prod"); }
    public boolean has(String profile) { return Arrays.asList(env.getActiveProfiles()).contains(profile); }
}
