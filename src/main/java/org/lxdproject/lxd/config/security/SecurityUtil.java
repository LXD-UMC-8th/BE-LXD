package org.lxdproject.lxd.config.security;

import org.lxdproject.lxd.apiPayload.code.exception.handler.AuthHandler;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;


public class SecurityUtil {

    /**
     * 현재 로그인된 사용자의 memberId를 반환합니다.
     * 인증되지 않은 경우 예외를 발생시킵니다.
     *
     * @return 로그인된 사용자의 memberId
     * @throws AuthHandler 인증 정보가 없거나 잘못된 경우
     */
    public static Long getCurrentMemberId() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthHandler(ErrorStatus.AUTHENTICATION_INFO_NOT_FOUND);
        }

        try {
            String principalStr = authentication.getName();
            return Long.valueOf(principalStr);
        } catch (Exception e) {
            throw new AuthHandler(ErrorStatus.INVALID_ACCESS_TOKEN);
        }
    }

}
