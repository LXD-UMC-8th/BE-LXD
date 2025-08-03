package org.lxdproject.lxd.config.security.jwt;

import lombok.RequiredArgsConstructor;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.lxdproject.lxd.apiPayload.code.exception.handler.AuthHandler;
import org.lxdproject.lxd.apiPayload.code.exception.handler.GeneralException;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.auth.enums.TokenType;
import org.lxdproject.lxd.config.security.SecurityConfig;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter{

    private final JwtTokenProvider jwtTokenProvider;
    private static final AntPathMatcher antPathMatcher = new AntPathMatcher();


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();

        // SecurityConfig의 화이트 리스트에 존재할 시, 토큰 인증이 필요 없으므로 바로 다음 필터로 이동
        if(isWhitelisted(uri)){
            filterChain.doFilter(request, response);
            return;
        }

        // 아래부터, 화이트 리스트에 존재하지 않을 시 과정(토큰 인증이 필요한 경우)

        // 토큰 추출
        String token = resolveToken(request);

        // request 헤더에 토큰이 존재하지 않을 경우 로그인이 필요하다는 에러 처리
        if(!StringUtils.hasText(token)){
            throw new AuthHandler(ErrorStatus.REQUIRED_LOGIN);
        }

        // 토큰의 올바른 값인지, 만료 시간 내인지 등을 검사 후 만족하지 않을 시 에러 처리
        jwtTokenProvider.validateAccessTokenOrThrow(token);

        // refresh 토큰일 경우, 토큰 에러 처리
        if(!jwtTokenProvider.getTokenType(token).equals(TokenType.ACCESS.name())){
            throw new AuthHandler(ErrorStatus.INVALID_ACCESS_TOKEN);
        }


        // 토큰 유효성 검사를 성공하면 이후 로직
        Authentication authentication = jwtTokenProvider.getAuthentication(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(JwtConstants.AUTH_HEADER);
        if(StringUtils.hasText(bearerToken) && bearerToken.startsWith(JwtConstants.TOKEN_PREFIX)) {
            return bearerToken.substring(JwtConstants.TOKEN_PREFIX.length());
        }
        return null;
    }

    private boolean isWhitelisted(String uri) {
        return Arrays.stream(SecurityConfig.WHITELIST)
                .anyMatch(pattern -> antPathMatcher.match(pattern, uri));
    }

}
