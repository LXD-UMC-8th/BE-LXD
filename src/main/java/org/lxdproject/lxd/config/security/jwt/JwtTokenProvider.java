package org.lxdproject.lxd.config.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.code.exception.handler.AuthHandler;
import org.lxdproject.lxd.apiPayload.code.exception.handler.GeneralException;
import org.lxdproject.lxd.apiPayload.code.exception.handler.MemberHandler;
import org.lxdproject.lxd.apiPayload.code.status.ErrorStatus;
import org.lxdproject.lxd.auth.enums.TokenType;
import org.lxdproject.lxd.config.properties.JwtProperties;
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

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
    }

    public String generateToken(Long memberId, String email, String role, TokenType tokenType) {

        return Jwts.builder()
                .setSubject(String.valueOf(memberId))
                .claim("role", role)
                .claim("email", email)
                .claim("type", tokenType.name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getAccessToken().getExpiration()))
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
    public void validateTokenOrThrow(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
        } catch (ExpiredJwtException e) {
            throw new GeneralException(ErrorStatus.EXPIRED_ACCESS_TOKEN);
        } catch (Exception e) {
            throw new GeneralException(ErrorStatus.INVALID_ACCESS_TOKEN);
        }
    }

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

        Long userId = Long.valueOf(claims.getSubject());
        String role = claims.get("role", String.class);

        // principal에 userId만 담을 수도 있고, UserDetailsService와 연계할 수도 있음
        User principal = new User(String.valueOf(userId), "", Collections.singleton(() -> role));

        return new UsernamePasswordAuthenticationToken(principal, token, principal.getAuthorities());
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

}
