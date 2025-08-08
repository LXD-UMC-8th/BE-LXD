package org.lxdproject.lxd.common.util;

public class ProfileUtil {

    public static boolean isLocalEnv() {
        return "local".equals(System.getProperty("spring.profiles.active"));
    }

    public static boolean isProdEnv() {
        return "prod".equals(System.getProperty("spring.profiles.active"));
    }

    public static boolean isProfile(String profile) {
        return profile != null && profile.equals(System.getProperty("spring.profiles.active"));
    }

    private ProfileUtil() {
    }
}
