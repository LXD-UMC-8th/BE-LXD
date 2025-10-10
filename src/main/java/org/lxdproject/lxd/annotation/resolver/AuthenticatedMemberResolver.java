package org.lxdproject.lxd.annotation.resolver;

import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.annotation.CurrentMember;
import org.lxdproject.lxd.apiPayload.code.exception.handler.MemberHandler;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.config.security.jwt.JwtConstants;
import org.lxdproject.lxd.config.security.jwt.JwtTokenProvider;
import org.lxdproject.lxd.member.entity.Member;
import org.lxdproject.lxd.member.repository.MemberRepository;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class AuthenticatedMemberResolver implements HandlerMethodArgumentResolver {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentMember.class)
                && Member.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) {
        // Authorization 헤더에서 Bearer 토큰 추출
        String header = webRequest.getHeader(JwtConstants.AUTH_HEADER);
        if (!StringUtils.hasText(header)) {
            throw new MemberHandler(ErrorStatus.REQUIRED_LOGIN);
        }
        String token = extractBearerToken(header);
        if (!StringUtils.hasText(token)) {
            throw new MemberHandler(ErrorStatus.INVALID_ACCESS_TOKEN);
        }

        // AT 유효성 검사 + 타입 검사
        jwtTokenProvider.validateAccessTokenOrThrow(token);
        String tokenType = jwtTokenProvider.getTokenType(token);
        if (!"ACCESS".equals(tokenType)) {
            throw new MemberHandler(ErrorStatus.INVALID_ACCESS_TOKEN);
        }

        // AT subject(= memberId)로 멤버 조회
        Long memberId = jwtTokenProvider.getMemberIdFromToken(token);
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND));
    }

    private String extractBearerToken(String header) {
        if (header.startsWith(JwtConstants.TOKEN_PREFIX)) {
            return header.substring(JwtConstants.TOKEN_PREFIX.length()).trim();
        }
        return null;
    }
}
