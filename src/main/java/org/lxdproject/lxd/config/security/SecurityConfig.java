package org.lxdproject.lxd.config.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.apiPayload.code.exception.SecurityExceptionHandler;
import org.lxdproject.lxd.config.security.jwt.JwtAuthenticationFilter;
import org.lxdproject.lxd.config.security.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // 인증 없이 접근 가능한 화이트리스트 경로 정의
    public static final String[] WHITELIST = {
            "/",
            "/health",
            "/test/**",
            "/members/join",
            "/members/check-username",
            "/members/check-username/**", // 쿼리 파라미터가 있는 경우 /** uri 추가로 붙여주기
            "/members/password-verify",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/auth/login",
            "/auth/google/login",
            "/auth/emails/verification-requests",
            "/auth/emails/verifications",
            "/auth/email",
            "/auth/email/**", // 쿼리 파라미터가 있는 경우 /** uri 추가로 붙여주기
            "/auth/reissue",
            "/auth/logout"
    };

    private final JwtTokenProvider jwtTokenProvider;
    private final SecurityExceptionHandler securityExceptionHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .cors(cors -> {})
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(WHITELIST).permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(securityExceptionHandler, JwtAuthenticationFilter.class);

        return http.build();
    }

    @Value("${urls.frontend}")
    private String frontendUrl;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(frontendUrl));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true); // Authorization 헤더 허용

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
