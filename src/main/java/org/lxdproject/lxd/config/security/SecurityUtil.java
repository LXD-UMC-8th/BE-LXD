package org.lxdproject.lxd.config.security;

import org.lxdproject.lxd.apiPayload.code.exception.handler.AuthHandler;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;


public class SecurityUtil {

    public static Long getCurrentMemberId() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthHandler(ErrorStatus.AUTHENTICATION_INFO_NOT_FOUND);
        }

        try {
            String principalStr = authentication.getName();
            return Long.valueOf(principalStr);
        } catch (Exception e) {
            throw new AuthHandler(ErrorStatus.INVALID_AUTHENTICATION_INFO);
        }
    }

}
