package org.lxdproject.lxd.auth.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.lxdproject.lxd.apiPayload.code.exception.handler.AuthHandler;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.auth.dto.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class WithdrawnAccountFilter extends OncePerRequestFilter {

    // 계정 복구 api 엔드포인트
    private static final String RECOVER_API = "/auth/recover";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 탈퇴 상태인 경우
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails principal) {
            if (principal.isDeleted()) {
                //TODO 복구 로직인지 검증하는 부분 추가하기

                throw new AuthHandler(ErrorStatus.WITHDRAWN_USER);
            }
        }

        filterChain.doFilter(request, response);
    }
}
