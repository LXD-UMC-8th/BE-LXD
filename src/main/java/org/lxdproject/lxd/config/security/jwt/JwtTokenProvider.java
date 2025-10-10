package org.lxdproject.lxd.config.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.code.exception.handler.AuthHandler;
import org.lxdproject.lxd.apiPayload.code.exception.handler.GeneralException;
import org.lxdproject.lxd.apiPayload.code.exception.handler.MemberHandler;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.auth.dto.CustomUserDetails;
import org.lxdproject.lxd.auth.enums.TokenType;
import org.lxdproject.lxd.config.properties.JwtProperties;
import org.lxdproject.lxd.config.security.SecurityUtil;
import org.lxdproject.lxd.member.repository.MemberRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Date;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private final MemberRepository memberRepository;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
    }

    public String generateToken(Long memberId, String email, String role, TokenType tokenType) {

        Long expiration;

        if(tokenType == TokenType.ACCESS){
            expiration = jwtProperties.getAccessToken().getExpiration();
        }else if(tokenType == TokenType.REFRESH){
            expiration = jwtProperties.getRefreshToken().getExpiration();
        }else{
            throw new AuthHandler(ErrorStatus.INVALID_AUTHENTICATION_INFO);
        }


        return Jwts.builder()
                .setSubject(String.valueOf(memberId))
                .claim("role", role)
                .claim("email", email)
                .claim("tokenType", tokenType.name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // 액세스 토큰 전용 유효성 검사 메서드
    public void validateAccessTokenOrThrow(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
        } catch (ExpiredJwtException e) {
            throw new AuthHandler(ErrorStatus.EXPIRED_ACCESS_TOKEN);
        } catch (Exception e) {
            throw new AuthHandler(ErrorStatus.INVALID_ACCESS_TOKEN);
        }
    }

    // 리프레쉬 토큰 전용 유효성 검사 메서드
    public void validateRefreshTokenOrThrow(String refreshToken){
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(refreshToken);
        } catch (ExpiredJwtException e) {
            throw new AuthHandler(ErrorStatus.EXPIRED_REFRESH_TOKEN);
        } catch (Exception e) {
            throw new AuthHandler(ErrorStatus.INVALID_REFRESH_TOKEN);
        }

    }


    public Authentication getAuthentication(String token) {

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        Long memberId = Long.valueOf(claims.getSubject());

        // principal에 userId만 담을 수도 있고, UserDetailsService와 연계할 수도 있음
        //User principal = new User(String.valueOf(userId), "", Collections.singleton(() -> role));

        CustomUserDetails customUserDetails = new CustomUserDetails(
                memberRepository.findById(memberId)
                        .orElseThrow(() -> new MemberHandler(ErrorStatus.MEMBER_NOT_FOUND))
                );

        return new UsernamePasswordAuthenticationToken(customUserDetails, token, customUserDetails.getAuthorities());
    }

    public static String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(JwtConstants.AUTH_HEADER);
        if(StringUtils.hasText(bearerToken) && bearerToken.startsWith(JwtConstants.TOKEN_PREFIX)) {
            return bearerToken.substring(JwtConstants.TOKEN_PREFIX.length());
        }
        return null;
    }

    public Authentication extractAuthentication(HttpServletRequest request){
        String accessToken = resolveToken(request);
        if(accessToken == null || !validateToken(accessToken)) {
            throw new MemberHandler(ErrorStatus.INVALID_ACCESS_TOKEN);
        }
        return getAuthentication(accessToken);
    }

    public String getTokenType(String token){
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody().get("tokenType", String.class);
    }

    public Long getMemberIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return Long.valueOf(claims.getSubject());
    }
}
